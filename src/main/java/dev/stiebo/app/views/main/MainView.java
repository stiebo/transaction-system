package dev.stiebo.app.views.main;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Home")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE)
@AnonymousAllowed
public class MainView extends Composite<VerticalLayout> {

    public MainView() {
        VerticalLayout layout = getContent();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        H1 title = new H1("Welcome to the Anti-Fraud System");

        Paragraph subTitle = new Paragraph("A powerful system to detect and prevent fraudulent " +
                "activity in online transactions.");

        Paragraph introText = new Paragraph(
                "The Anti-Fraud System provides real-time detection and prevention of potentially fraudulent " +
                        "activities. It helps businesses mitigate risk by leveraging advanced algorithms, blacklists," +
                        " and transaction scoring. With a simple and intuitive interface, you can review suspicious " +
                        "transactions and take immediate action."
        );
        introText.setWidth("70%");

        HorizontalLayout featureCards = createFeatureCards();

        Button getStartedButton = new Button("Get Started"); // , new Icon(VaadinIcon.ARROW_FORWARD));
        getStartedButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("login"))
        );

        layout.add(title, subTitle, introText, featureCards, getStartedButton);
    }

    private HorizontalLayout createFeatureCards() {
        Div detectionCard = createFeatureCard(
                LineAwesomeIconUrl.EYE,
                "Real-Time Fraud Detection",
                "Monitor transactions in real-time to identify and flag suspicious activity as it happens."
        );

        Div blacklistCard = createFeatureCard(
                LineAwesomeIconUrl.LOCK_SOLID,
                "Blacklist Management",
                "Easily manage blacklisted IPs, card numbers, and regions to reduce exposure to known threats."
        );

        Div userManagementCard = createFeatureCard(
                LineAwesomeIconUrl.USERS_SOLID,
                "User Management & Roles",
                "Manage user roles and permissions to control access to sensitive features and ensure security."
        );

        HorizontalLayout featureCards = new HorizontalLayout(detectionCard, blacklistCard, userManagementCard);
        featureCards.setWidthFull();
        featureCards.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        featureCards.setSpacing(true);
        featureCards.setPadding(true);
        featureCards.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        featureCards.setFlexShrink(1, detectionCard, blacklistCard, userManagementCard);
        featureCards.getStyle().set("flex-wrap", "wrap");
        return featureCards;
    }

    private Div createFeatureCard(String iconUrl, String title, String description) {
        Image icon = new Image(iconUrl, title);
        icon.setWidth("48px");
        icon.setHeight("48px");

        H3 cardTitle = new H3(title);
        Paragraph cardDescription = new Paragraph(description);

        VerticalLayout cardContent = new VerticalLayout(icon, cardTitle, cardDescription);
        cardContent.setSpacing(false);
        cardContent.setAlignItems(FlexComponent.Alignment.CENTER);
        cardContent.addClassName(LumoUtility.BorderRadius.MEDIUM);
        cardContent.addClassName(LumoUtility.BoxShadow.LARGE);

        Div card = new Div(cardContent);
        card.setWidth("250px");
        card.setHeight("300px");
        card.addClassName(LumoUtility.Margin.MEDIUM);
        return card;
    }
}
