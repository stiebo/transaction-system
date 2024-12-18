package dev.stiebo.app.views.Transactions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import dev.stiebo.app.data.Transaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionsFilter extends HorizontalLayout implements Specification<Transaction> {
    private final DatePicker startDate;
    private final DatePicker endDate;
    private final TextField minAmount;
    private final TextField maxAmount;
    private final TextField textSearch;
    private final Button resetBtn;

    public TransactionsFilter(Runnable onSearch) {
        setWidthFull();
        addClassName(LumoUtility.Gap.SMALL);
        addClassName(LumoUtility.Padding.SMALL);
        setAlignItems(FlexComponent.Alignment.END);
        getStyle().set("flex-wrap", "wrap");

        startDate = new DatePicker("Start Date");
        endDate = new DatePicker("End Date");
        minAmount = new TextField("Min Amount");
        maxAmount = new TextField("Max Amount");
        textSearch = new TextField("Other");
        resetBtn = new Button("Reset", e -> resetFilters(onSearch));

        startDate.addValueChangeListener(e -> onSearch.run());
        endDate.addValueChangeListener(e -> onSearch.run());
        minAmount.addValueChangeListener(e -> onSearch.run());
        maxAmount.addValueChangeListener(e -> onSearch.run());
        textSearch.addValueChangeListener(e -> onSearch.run()); // Triggers search on value change

        add(startDate, endDate, minAmount, maxAmount, textSearch, resetBtn);
    }

    private void resetFilters(Runnable onSearch) {
        startDate.clear();
        endDate.clear();
        minAmount.clear();
        maxAmount.clear();
        textSearch.clear();
        onSearch.run();
    }

    @Override
    public Predicate toPredicate(Root<Transaction> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (startDate.getValue() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("date"), startDate.getValue().atStartOfDay()));
        }
        if (endDate.getValue() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get("date"), endDate.getValue().atTime(LocalTime.MAX)));
        }

        if (!minAmount.isEmpty()) {
            try {
                long min = Long.parseLong(minAmount.getValue());
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), min));
            } catch (NumberFormatException ignored) {
            }
        }
        if (!maxAmount.isEmpty()) {
            try {
                long max = Long.parseLong(maxAmount.getValue());
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), max));
            } catch (NumberFormatException ignored) {
            }
        }

        if (!textSearch.isEmpty()) {
            String value = textSearch.getValue().toLowerCase();
            Predicate numberPredicate = cb.like(cb.lower(root.get("number")), "%" + value + "%");
            Predicate ipPredicate = cb.like(cb.lower(root.get("ip")), "%" + value + "%");
            Predicate resultPredicate = cb.like(cb.lower(root.get("result")), "%" + value + "%");
            Predicate regionPredicate = cb.like(cb.lower(root.get("region")), "%" + value + "%");
            Predicate feedbackPredicate = cb.like(cb.lower(root.get("feedback")), "%" + value + "%");
            predicates.add(cb.or(regionPredicate, numberPredicate, ipPredicate, resultPredicate, feedbackPredicate));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
