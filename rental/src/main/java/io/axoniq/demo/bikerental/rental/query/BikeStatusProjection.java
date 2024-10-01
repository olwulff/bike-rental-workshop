package io.axoniq.demo.bikerental.rental.query;

import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import io.axoniq.demo.bikerental.coreapi.rental.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import io.axoniq.demo.bikerental.coreapi.rental.RentalStatus;

@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository) {
        this.bikeStatusRepository = bikeStatusRepository;
    }

    @EventHandler
    void on(BikeRegisteredEvent event) {
        BikeStatus bs = new BikeStatus(event.bikeId(), event.bikeType(), event.location());
        bikeStatusRepository.save(bs);
    }

    @EventHandler
    void on(BikeRequestedEvent event) {
        
        Optional<BikeStatus> bikeStatus = bikeStatusRepository.findById(event.bikeId());
        if (bikeStatus.isPresent()) {
            bikeStatus.get().requestedBy(event.renter());
            bikeStatusRepository.save(bikeStatus.get());
        }
    }

    @QueryHandler(queryName = "findAll")
    public Iterable<BikeStatus> findAll() {
        return bikeStatusRepository.findAll();
    }

    @QueryHandler(queryName = "findOne")
    public BikeStatus findOne(String bikeId) {
        return bikeStatusRepository.findById(bikeId).orElse(null);
    }

}
