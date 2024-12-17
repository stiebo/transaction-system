package dev.stiebo.app.views.users;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("users")
@RolesAllowed("ADMIN")
public class UsersView extends VerticalLayout {
}
