package dev.stiebo.app.views.suspiciousip;

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
import dev.stiebo.app.data.SuspiciousIp;
import dev.stiebo.app.services.SuspiciousIpService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Suspicious IPs")
@Route("suspiciousips")
@Menu(order = 4, icon = LineAwesomeIconUrl.NETWORK_WIRED_SOLID)
@RolesAllowed("SUPPORT")
public class SuspiciousIpsView extends Composite<VerticalLayout> {

    private final SuspiciousIpService service;

    private SuspiciousIpsFilterAddNew filterAddNew;
    private final Grid<SuspiciousIp> grid;

    public SuspiciousIpsView(SuspiciousIpService service) {
        this.service = service;

        VerticalLayout layout = getContent();
        layout.setSizeFull();

        filterAddNew = new SuspiciousIpsFilterAddNew(service, () -> refreshGrid(filterAddNew));

        grid = new Grid<>();
        grid.setWidthFull();
        grid.addColumn(SuspiciousIp::getIp).setHeader("Suspicious IP");
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, suspiciousIp) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> deleteIp(suspiciousIp));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setHeader("Manage");

        layout.add(filterAddNew, grid);
        refreshGrid(filterAddNew);
    }

    private void refreshGrid(SuspiciousIpsFilterAddNew filterAddNew) {
        grid.setItems(query ->
                service.list(
                        PageRequest.of(query.getPage(), query.getPageSize()), filterAddNew)
                        .stream()
                );
        grid.scrollToStart();
    }

    private void deleteIp(SuspiciousIp suspiciousIp) {
        service.delete(suspiciousIp);
        refreshGrid(filterAddNew);
    }
}
