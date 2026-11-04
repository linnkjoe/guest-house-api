package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.BookingResponseDTO;
import com.mabjaiainn.proj.DTO.CustomPackRequest;
import com.mabjaiainn.proj.DTO.CustomPackResponse;
import com.mabjaiainn.proj.DTO.QuoteResponse;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.CustomPackage;
import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.entity.PaymentMethod;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.CustomPackageRepository;
import com.mabjaiainn.proj.repository.PackFeatureRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Historico de estadias personalizadas de cada cliente e reserva a partir delas. */
@Service
public class CustomPackService {

    private static final int MAX_PER_CLIENT = 30;

    private final CustomPackageRepository repository;
    private final ClientRepository clientRepository;
    private final PackFeatureRepository featureRepository;
    private final PacksRepository packsRepository;
    private final PricingService pricingService;
    private final BookingService bookingService;

    public CustomPackService(CustomPackageRepository repository, ClientRepository clientRepository,
            PackFeatureRepository featureRepository, PacksRepository packsRepository,
            PricingService pricingService, BookingService bookingService) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.featureRepository = featureRepository;
        this.packsRepository = packsRepository;
        this.pricingService = pricingService;
        this.bookingService = bookingService;
    }

    @Transactional(readOnly = true)
    public List<CustomPackResponse> list(Long clientId) throws BusinessException {
        List<CustomPackResponse> result = new java.util.ArrayList<>();
        for (CustomPackage item : repository.findByClient_ClientIdOrderByLastUsedAtDescCreatedAtDesc(clientId)) {
            result.add(toResponse(item));
        }
        return result;
    }

    @Transactional
    public CustomPackResponse create(Long clientId, CustomPackRequest request) throws BusinessException {
        var client = clientRepository.findById(clientId).orElseThrow(() -> new SourceNotFound("Client Not found"));
        String name = request.name().trim();
        if (repository.countByClient_ClientId(clientId) >= MAX_PER_CLIENT) {
            throw new BusinessException("History is full, remove an old personalized stay first");
        }
        if (repository.existsByClient_ClientIdAndNameIgnoreCase(clientId, name)) {
            throw new BusinessException("You already have a stay with this name");
        }
        QuoteResponse quote = pricingService.quote(request.duration(), request.featureIds());
        CustomPackage item = new CustomPackage();
        item.setClient(client);
        fill(item, name, quote);
        return toResponse(repository.save(item));
    }

    @Transactional
    public CustomPackResponse update(Long clientId, Long customId, CustomPackRequest request) throws BusinessException {
        CustomPackage item = find(clientId, customId);
        String name = request.name().trim();
        if (!item.getName().equalsIgnoreCase(name)
                && repository.existsByClient_ClientIdAndNameIgnoreCase(clientId, name)) {
            throw new BusinessException("You already have a stay with this name");
        }
        fill(item, name, pricingService.quote(request.duration(), request.featureIds()));
        return toResponse(repository.save(item));
    }

    @Transactional
    public void delete(Long clientId, Long customId) {
        repository.delete(find(clientId, customId));
    }

    /**
     * Reserva usando uma estadia guardada. Cria uma copia (snapshot) do pacote com o preco de hoje,
     * para que reservas antigas nao mudem se a equipa alterar precos. Se a reserva falhar
     * (ex.: sem quartos), tudo e desfeito, incluindo a copia.
     */
    @Transactional(rollbackFor = Exception.class)
    public BookingResponseDTO book(Long clientId, Long customId, LocalDateTime bookingEnter) throws BusinessException {
        return book(clientId, customId, bookingEnter, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookingResponseDTO book(Long clientId, Long customId, LocalDateTime bookingEnter,
            PaymentMethod paymentMethod) throws BusinessException {
        CustomPackage item = find(clientId, customId);
        QuoteResponse quote = pricingService.quote(item.getDuration(), idsOf(item.getFeatures()));

        Set<PackFeature> features = new LinkedHashSet<>(featureRepository.findAllById(quote.featureIds()));
        Packs snapshot = new Packs();
        snapshot.setCustom(true);
        String snapshotName = "Personalizada · " + item.getName();
        snapshot.setPackName(snapshotName.length() > 60 ? snapshotName.substring(0, 60) : snapshotName);
        snapshot.setDescription(describe(features));
        snapshot.setDuration(quote.duration());
        snapshot.setPackPrice(quote.total());
        snapshot.setFeatures(features);
        snapshot.setPackAc(features.stream().anyMatch(f -> PackFeature.CODE_AC.equals(f.getCode())));
        snapshot = packsRepository.save(snapshot);

        BookingResponseDTO booking = bookingService.createBookingForClient(
                new Booking(bookingEnter), clientId, snapshot.getPackId(), paymentMethod);

        item.setUseCount(item.getUseCount() + 1);
        item.setLastUsedAt(LocalDateTime.now());
        item.setLastPrice(quote.total());
        repository.save(item);
        return booking;
    }

    private CustomPackage find(Long clientId, Long customId) {
        return repository.findByCustomIdAndClient_ClientId(customId, clientId)
                .orElseThrow(() -> new SourceNotFound("Personalized stay not found"));
    }

    private void fill(CustomPackage item, String name, QuoteResponse quote) {
        item.setName(name);
        item.setDuration(quote.duration());
        item.setFeatures(new LinkedHashSet<>(featureRepository.findAllById(quote.featureIds())));
        item.setLastPrice(quote.total());
    }

    private CustomPackResponse toResponse(CustomPackage item) throws BusinessException {
        List<Long> ids = idsOf(item.getFeatures());
        // Preco de hoje: pode ter mudado desde que foi guardada.
        QuoteResponse quote;
        try {
            quote = pricingService.quote(item.getDuration(), ids);
        } catch (BusinessException ex) {
            // Extra desativado ou catalogo alterado: mostra a estadia sem preco atual.
            return new CustomPackResponse(item.getCustomId(), item.getName(), item.getDuration(), ids,
                    item.getFeatures().stream().map(PackFeature::getName).toList(),
                    item.getLastPrice(), false, item.getUseCount(), item.getCreatedAt(), item.getLastUsedAt());
        }
        boolean changed = item.getLastPrice() != null && item.getLastPrice().compareTo(quote.total()) != 0;
        return new CustomPackResponse(item.getCustomId(), item.getName(), item.getDuration(), quote.featureIds(),
                featureRepository.findAllById(quote.featureIds()).stream().map(PackFeature::getName).toList(),
                quote.total(), changed, item.getUseCount(), item.getCreatedAt(), item.getLastUsedAt());
    }

    private static List<Long> idsOf(Set<PackFeature> features) {
        return features.stream().map(PackFeature::getFeatureId).toList();
    }

    private static String describe(Set<PackFeature> features) {
        if (features.isEmpty()) return "Estadia personalizada";
        return "Inclui: " + String.join(", ", features.stream().map(PackFeature::getName).toList());
    }
}
