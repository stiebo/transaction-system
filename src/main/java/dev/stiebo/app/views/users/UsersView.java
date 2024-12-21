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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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
        grid.addColumn(UserDto::getName).setHeader("Name");
        grid.addColumn(UserDto::getUsername).setHeader("Username");
        grid.addColumn(UserDto::getRoleName).setHeader("Role");
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
                authenticatedUser.get().get().getUsername().equals(userDto.getUsername())) {
            Notification notification = new Notification("Own user cannot be deleted.",
                    5000, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();
            return;
        }
        userService.deleteUser(userDto);
        refreshGrid();
    }

    private void openAddUserDialog() {
        Binder<UserDto> binder = new Binder<>();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add New User");

        TextField nameField = new TextField("Name");
        nameField.setRequired(true);
        binder.forField(nameField)
                .asRequired()
                .bind(UserDto::getName, UserDto::setName);

        TextField usernameField = new TextField("Username");
        usernameField.setRequired(true);
        binder.forField(usernameField)
                .asRequired()
                .bind(UserDto::getUsername, UserDto::setUsername);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        binder.forField(passwordField)
                .asRequired()
                .bind(UserDto::getPassword, UserDto::setPassword);

        ComboBox<RoleName> roleComboBox = new ComboBox<>("Role");
        roleComboBox.setItems(RoleName.values());
        roleComboBox.setRequired(true);
        binder.forField(roleComboBox)
                .asRequired()
                .bind(UserDto::getRoleName, UserDto::setRoleName);

        FormLayout dialogLayout = new FormLayout(nameField, usernameField, passwordField, roleComboBox);

        Button saveButton = new Button("Save", event -> {
            UserDto userDto = new UserDto();

            try {
                binder.writeBean(userDto);
                userService.createUser(userDto);
                Notification notification = new Notification("User created successfully!",
                        5000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.open();
                refreshGrid();
                dialog.close();
            } catch (Exception e) {
                Notification notification = new Notification("Error: " + e.getMessage(),
                        5000, Notification.Position.TOP_CENTER);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        dialog.add(dialogLayout, buttons);

        dialog.open();
        nameField.focus();
    }

}
