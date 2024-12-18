package dev.stiebo.app.views.stolencards;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.stiebo.app.data.StolenCard;
import dev.stiebo.app.services.StolenCardService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Stolen cards")
@Route("stolencards")
@Menu(order = 3, icon = LineAwesomeIconUrl.COMMENTS_DOLLAR_SOLID)
@RolesAllowed("SUPPORT")
public class StolenCardsView extends Composite<VerticalLayout> {

    private final StolenCardService service;

    private StolenCardsFilterAddNew filterAddNew;
    private Grid<StolenCard> grid;

    public StolenCardsView(StolenCardService service) {
        this.service = service;

        VerticalLayout layout = getContent();
        layout.setSizeFull();

        filterAddNew = new StolenCardsFilterAddNew(service, () -> refreshGrid(filterAddNew));

        grid = new Grid<>();
        grid.setWidthFull();
        grid.addColumn(StolenCard::getNumber).setHeader("Stolen card");
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, stolenCard) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> deleteIp(stolenCard));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setHeader("Manage");

        layout.add(filterAddNew, grid);
        refreshGrid(filterAddNew);
    }

    private void refreshGrid(StolenCardsFilterAddNew filterAddNew) {
        grid.setItems(query ->
                service.list(
                                PageRequest.of(query.getPage(), query.getPageSize()), filterAddNew)
                        .stream()
        );
        grid.scrollToStart();
    }

    private void deleteIp(StolenCard stolenCard) {
        service.delete(stolenCard);
        refreshGrid(filterAddNew);
    }
}
