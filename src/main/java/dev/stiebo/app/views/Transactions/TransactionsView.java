package dev.stiebo.app.views.Transactions;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.stiebo.app.data.Transaction;
import dev.stiebo.app.services.TransactionService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Transactions")
@Route("transactions")
@Menu(order = 2, icon = LineAwesomeIconUrl.COMMENTS_DOLLAR_SOLID)
@RolesAllowed("SUPPORT")
public class TransactionsView extends Composite<VerticalLayout> {

    private final TransactionService transactionService;

    private final Grid<Transaction> grid;
    private TransactionsFilter filters;

    public TransactionsView(TransactionService transactionService) {
        this.transactionService = transactionService;


        // Main layout configuration
        VerticalLayout layout = getContent();
        layout.setSizeFull();

        // Create the Filters component
        filters = new TransactionsFilter(() -> refreshGrid(filters));

        // Create the grid
        grid = new Grid<>();
        grid.setWidthFull();
        grid.addColumn(new LocalDateTimeRenderer<>(
                Transaction::getDate,"MM/dd/yyyy HH:mm:ss")).setHeader("Date");
        grid.addColumn(Transaction::getAmount).setHeader("Amount");
        grid.addColumn(Transaction::getNumber).setHeader("Number");
        grid.addColumn(Transaction::getIp).setHeader("IP");
        grid.addColumn(Transaction::getRegion).setHeader("Region");
        grid.addColumn(Transaction::getResult).setHeader("Result");
        grid.addColumn(Transaction::getFeedback).setHeader("Feedback");

        // Add Filters and Grid to the main layout
        layout.add(filters, grid);

        // Initial load of the data
        refreshGrid(filters);
    }

    /**
     * Refreshes the grid with the current filter values.
     *
     * @param filters the Filters instance containing filter input values
     */
    private void refreshGrid(TransactionsFilter filters) {
        grid.setItems(query ->
                transactionService.list(
                        PageRequest.of(query.getPage(), query.getPageSize()),
                        filters
                ).stream()
        );
        grid.scrollToStart();
    }
}
