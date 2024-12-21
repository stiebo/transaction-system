package dev.stiebo.app.views.suspiciousip;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import dev.stiebo.app.data.SuspiciousIp;
import dev.stiebo.app.services.SuspiciousIpService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class SuspiciousIpsFilterAddNew extends HorizontalLayout implements Specification<SuspiciousIp> {
    private final SuspiciousIpService service;
    private final TextField suspiciousIpFilter;

    public SuspiciousIpsFilterAddNew(SuspiciousIpService service, Runnable onUpdate) {
        this.service = service;
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.END);

        suspiciousIpFilter = new TextField("Suspicious Ip");
        Button resetBtn = new Button("Reset", event -> resetFilter(onUpdate));
        Button addNewBtn = new Button("Add new", event -> openAddNewDialog(onUpdate));

        suspiciousIpFilter.addValueChangeListener(event -> onUpdate.run());

        add(suspiciousIpFilter, resetBtn, addNewBtn);
    }

    private void resetFilter(Runnable onUpdate) {
        suspiciousIpFilter.clear();
        onUpdate.run();
    }

    private void openAddNewDialog(Runnable onUpdate) {
        Binder<SuspiciousIp> binder = new Binder<>(SuspiciousIp.class);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add new suspicious IP");

        TextField suspiciousIpField = new TextField("IP Address");

        binder.forField(suspiciousIpField)
                .asRequired("IP is required")
                .withValidator(new RegexpValidator("Please enter a valid IPv4 address (e.g., 192.168.0.1).",
                        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"))
                .bind(SuspiciousIp::getIp, SuspiciousIp::setIp);

        Button saveButton = new Button("Save", event -> {
            SuspiciousIp suspiciousIp = new SuspiciousIp();
            try {
                binder.writeBean(suspiciousIp);
                service.create(suspiciousIp);
                Notification notification = new Notification("Entry created successfully!",
                        5000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.open();
                onUpdate.run();
                dialog.close();
            } catch (Exception e) {
                Notification notification = new Notification("Error: " + e.getMessage(),
                        5000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });
        saveButton.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(suspiciousIpField, buttons);

        dialog.open();
        suspiciousIpField.focus();
    }

    @Override
    public Predicate toPredicate(Root<SuspiciousIp> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (!suspiciousIpFilter.isEmpty()) {
            String value = suspiciousIpFilter.getValue();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("ip")), "%" + value + "%");
        }
        return criteriaBuilder.conjunction();
    }
}
