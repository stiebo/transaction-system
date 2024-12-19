package dev.stiebo.app.views.users;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import dev.stiebo.app.configuration.RoleName;
import dev.stiebo.app.dtos.UserDto;
import dev.stiebo.app.security.AuthenticatedUser;
import dev.stiebo.app.services.UserService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Arrays;

@PageTitle("Users")
@Route("users")
@Menu(order = 2, icon = LineAwesomeIconUrl.USERS_SOLID)
@RolesAllowed("ADMIN")
public class UsersView extends Composite<VerticalLayout> {

    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;

    private final Grid<UserDto> grid;

    public UsersView(UserService userService, AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        VerticalLayout layout = getContent();
        layout.setSizeFull();

        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.END);
        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Button addUserBtn = new Button("Add User", event -> openAddUserDialog());
        topRow.add(searchField, addUserBtn);

        grid = new Grid<>();
        grid.addColumn(UserDto::name).setHeader("Name");
        grid.addColumn(UserDto::username).setHeader("Username");
        grid.addColumn(UserDto::roleName).setHeader("Role");
        grid.addColumn(
                new ComponentRenderer<>(Button::new, (button, userDto) -> {
                    button.addThemeVariants(ButtonVariant.LUMO_ICON,
                            ButtonVariant.LUMO_ERROR,
                            ButtonVariant.LUMO_TERTIARY);
                    button.addClickListener(e -> deleteUser(userDto));
                    button.setIcon(new Icon(VaadinIcon.TRASH));
                })).setHeader("Manage");

        CallbackDataProvider<UserDto, String> dataProvider = DataProvider.fromFilteringCallbacks(
                query -> {
                    String filter = query.getFilter().orElse("");
                    return userService.listWithFilter(filter, PageRequest.of(query.getPage(), query.getPageSize(),
                                    VaadinSpringDataHelpers.toSpringDataSort(query)))
                            .stream();
                },
                query -> {
                    String filter = query.getFilter().orElse("");
                    return userService.countWithFilter(filter); // Count total items based on filter
                }
        );
        ConfigurableFilterDataProvider<UserDto, Void, String> filterDataProvider = dataProvider
                .withConfigurableFilter();
        searchField.addValueChangeListener(event -> {
            filterDataProvider.setFilter(event.getValue());
            grid.scrollToStart();
        });
        grid.setDataProvider(filterDataProvider);

        layout.add(topRow, grid);

    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void deleteUser(UserDto userDto) {
        if (authenticatedUser.get().isPresent() &&
                authenticatedUser.get().get().getUsername().equals(userDto.username())) {
            Notification.show("Own user cannot be deleted.", 3000, Notification.Position.TOP_CENTER);
            return;
        }
        userService.deleteUser(userDto);
        refreshGrid();
    }

    private void openAddUserDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New User");

        TextField nameField = new TextField("Name");
        nameField.setRequired(true);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);

        ComboBox<String> roleComboBox = new ComboBox<>("Role");
        roleComboBox.setItems(Arrays.stream(RoleName.values()).map(Enum::name).toList());
        roleComboBox.setRequired(true);

        FormLayout dialogLayout = new FormLayout(nameField, usernameField, passwordField, roleComboBox);

        Button saveButton = new Button("Save", event -> {
            String name = nameField.getValue();
            String username = usernameField.getValue();
            String password = passwordField.getValue();
            String role = roleComboBox.getValue();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
                Notification.show("Please fill out all required fields.", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                userService.createUser(name, username, password, role);
                Notification.show("User created successfully!", 3000, Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
            refreshGrid();
            dialog.close();
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(dialogLayout, buttons);

        dialog.open();
        nameField.focus();
    }

}
