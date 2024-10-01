package io.axoniq.demo.bikerental.rental.command;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;

import com.google.common.base.Objects;

import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeInUseEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeReturnedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RegisterBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.ReturnBikeCommand;

@Aggregate
public class Bike {

    @AggregateIdentifier
    private String bikeId;

    private String renter;
    private String rentalReference;
    private boolean isAvailable;

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.ALWAYS)
    public void handle (RegisterBikeCommand command) {
        AggregateLifecycle.apply(new BikeRegisteredEvent(command.bikeId(), command.bikeType(), command.location()));
    }

    @EventSourcingHandler
    void on(BikeRegisteredEvent event) {
        bikeId = event.bikeId();
        isAvailable = true;
    }

    @CommandHandler
    public String handle(RequestBikeCommand command) {
        if (!isAvailable) {
            throw new IllegalStateException(String.format("Bike {%s} is already requested", command.bikeId()));
        }
        String rentalReference = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new BikeRequestedEvent(command.bikeId(), command.renter(), rentalReference));
        return rentalReference;
    }

    @EventSourcingHandler
    void on(BikeRequestedEvent event) {
        this.rentalReference = event.rentalReference();
        this.renter = event.renter();
        this.isAvailable = false;
    }

    @CommandHandler
    public void handle(ReturnBikeCommand command) {
        if (isAvailable) {
            throw new IllegalStateException(String.format("Bike {%s} is not rent", command.bikeId()));
        }
        AggregateLifecycle.apply(new BikeReturnedEvent(command.bikeId(), command.location()));
    }

    @EventSourcingHandler
    void on(BikeReturnedEvent event) {
        this.isAvailable = true;
        this.rentalReference = null;
        this.renter = null;
    }

    @CommandHandler
    public void handle(ApproveRequestCommand command) {
        if (!Objects.equal(this.renter,command.renter())) {
            return;
        }
        AggregateLifecycle.apply(new BikeInUseEvent(command.bikeId(), command.renter()));
    }

    @EventSourcingHandler
    void on(BikeInUseEvent event) {
    }


    @CommandHandler
    public void handle(RejectRequestCommand command) {
        if (!Objects.equal(this.renter,command.renter())) {
            return;
        }
        AggregateLifecycle.apply(new RequestRejectedEvent(command.bikeId()));
    }

    @EventSourcingHandler
    void on(RequestRejectedEvent event) {
        this.isAvailable = true;
        this.rentalReference = null;
        this.renter = null;
    }
    

}
