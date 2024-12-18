package dev.stiebo.app.views.stolencards;


import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import dev.stiebo.app.data.StolenCard;
import dev.stiebo.app.services.StolenCardService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class StolenCardsFilterAddNew extends HorizontalLayout implements Specification<StolenCard> {
    private final StolenCardService service;
    private final TextField stolenCardFilter;

    public StolenCardsFilterAddNew(StolenCardService service, Runnable onUpdate) {
        this.service = service;
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.END);

        stolenCardFilter = new TextField("Stolen card");
        Button resetBtn = new Button("Reset", event -> resetFilter(onUpdate));
        Button addNewBtn = new Button("Add new", event -> openAddNewDialog(onUpdate));

        stolenCardFilter.addValueChangeListener(event -> onUpdate.run());

        add(stolenCardFilter, resetBtn, addNewBtn);
    }

    private void resetFilter(Runnable onUpdate) {
        stolenCardFilter.clear();
        onUpdate.run();
    }

    private void openAddNewDialog(Runnable onUpdate) {
        Binder<StolenCard> binder = new Binder<>(StolenCard.class);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add new stolen card");

        TextField stolenCardField = new TextField("Stolen card");

        binder.forField(stolenCardField)
                .asRequired("Card number required")
                .withValidator(value -> value.matches("\\d+"), "Number must be numeric")
                .withValidator(StolenCard::luhnCheck, "The number is not a valid credit card number.")
                .bind(StolenCard::getNumber, StolenCard::setNumber);

        Button saveButton = new Button("Save", event -> {
            StolenCard stolenCard = new StolenCard();
            try {
                binder.writeBean(stolenCard);
                service.create(stolenCard);
                Notification.show("Entry created successfully!", 3000, Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
            onUpdate.run();
            dialog.close();
        });
        saveButton.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(stolenCardField, buttons);

        dialog.open();
        stolenCardField.focus();
    }

    @Override
    public Predicate toPredicate(Root<StolenCard> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (!stolenCardFilter.isEmpty()) {
            String value = stolenCardFilter.getValue();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("number")), "%" + value + "%");
        }
        return criteriaBuilder.conjunction();
    }
}