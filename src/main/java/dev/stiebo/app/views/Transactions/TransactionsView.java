package dev.stiebo.app.views.Transactions;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import dev.stiebo.app.configuration.TransactionStatus;
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

    private final GridPro<Transaction> grid;
    private TransactionsFilter filters;

    public TransactionsView(TransactionService transactionService) {
        this.transactionService = transactionService;

        VerticalLayout layout = getContent();
        layout.setSizeFull();

        filters = new TransactionsFilter(() -> refreshGrid(filters));

        grid = new GridPro<>();
        grid.setWidthFull();
        grid.addColumn(new LocalDateTimeRenderer<>(
                Transaction::getDate,"MM/dd/yyyy HH:mm:ss")).setHeader("Date");
        grid.addColumn(Transaction::getAmount).setHeader("Amount");
        grid.addColumn(Transaction::getNumber).setHeader("Number");
        grid.addColumn(Transaction::getIp).setHeader("IP");
        grid.addColumn(Transaction::getRegion).setHeader("Region");
        grid.addColumn(Transaction::getResult)
                .setRenderer(new ComponentRenderer<>(transaction -> {
                    Span result = new Span(transaction.getResult().toString());
                    result.getElement().getThemeList().add(switch (transaction.getResult()) {
                        case ALLOWED -> "badge success";
                        case MANUAL_PROCESSING -> "badge";
                        case PROHIBITED -> "badge error";
                    });
                    return result;
                }))
                .setHeader("Result");
        grid.addEditColumn(Transaction::getFeedback)
                .withCellEditableProvider(transaction -> transaction.getFeedback() == null)
                .select((transaction, newFeedback) -> {
                    try {
                        transactionService.updateTransactionFeedback(transaction.getId(), newFeedback);
                        Notification.show("Feedback updated", 2000, Notification.Position.MIDDLE);
                        // need to also update "in-memory"-Transaction with new Feedback to show on screen
                        transaction.setFeedback(newFeedback);
                    }
                    catch (Exception e) {
                        Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                    }
                }, TransactionStatus.class)
                .setHeader("Feedback");
        grid.setEditOnClick(true);
        grid.addThemeVariants(GridProVariant.LUMO_HIGHLIGHT_EDITABLE_CELLS);

        layout.add(filters, grid);
        refreshGrid(filters);
    }

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
