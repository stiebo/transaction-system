package dev.stiebo.app.views.NewTransaction;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import dev.stiebo.app.configuration.Region;
import dev.stiebo.app.configuration.TransactionStatus;
import dev.stiebo.app.data.StolenCard;
import dev.stiebo.app.data.Transaction;
import dev.stiebo.app.dtos.PostTransactionFeedback;
import dev.stiebo.app.services.TransactionService;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

import static dev.stiebo.app.data.StolenCard.luhnCheck;

@PageTitle("New transaction")
@Route("newtransaction")
@Menu(order = 3, icon = LineAwesomeIconUrl.COMMENTS_DOLLAR_SOLID)
@RolesAllowed("MERCHANT")
public class NewTransactionView extends Composite<VerticalLayout> {
    private final TransactionService transactionService;

    private final Binder<Transaction> binder = new Binder<>(Transaction.class);

    private final TextField amountField = new TextField("Amount");
    private final TextField ipField = new TextField("IP Address");
    private final TextField numberField = new TextField("Number");
    private final ComboBox<Region> regionField = new ComboBox<>("Region");
    private final DateTimePicker dateField = new DateTimePicker("Transaction Date");

    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel");
    private final Button randomButton = new Button("Random");

    public NewTransactionView(TransactionService transactionService) {
        this.transactionService = transactionService;

        configureFormFields();
        configureBinder();

        getContent().setMaxWidth("950px");
        getContent().add(new H1("Create New Transaction"), createFormLayout(), createButtonLayout());
    }

    private void configureFormFields() {
        amountField.setPlaceholder("Enter amount");
        amountField.setPattern("\\d*");
        ipField.setPlaceholder("Enter IPv4 address");
        ipField.setPattern("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
        numberField.setPlaceholder("Enter number");
        numberField.setPattern("\\d*");
        regionField.setPlaceholder("Enter region");
        regionField.setItems(Region.values());
        dateField.setDatePlaceholder("Select date");
        dateField.setTimePlaceholder("Select time");
    }

    private void configureBinder() {
        binder.forField(amountField)
                .asRequired("Amount is required")
                .withConverter(new com.vaadin.flow.data.converter.StringToLongConverter("Amount must be a whole number"))
                .withValidator(amount -> amount >= 1, "Amount must be greater than or equal to 1")
                .bind(Transaction::getAmount, Transaction::setAmount);

        binder.forField(ipField)
                .asRequired("IP Address is required")
                .withValidator(new RegexpValidator("Please enter a valid IPv4 address (e.g., 192.168.0.1).",
                        "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"))
                .bind(Transaction::getIp, Transaction::setIp);

        binder.forField(numberField)
                .asRequired("Number is required")
                .withValidator(value -> value.matches("\\d+"), "Number must be numeric")
                .withValidator(StolenCard::luhnCheck, "The number is not a valid credit card number.")
                .bind(Transaction::getNumber, Transaction::setNumber);

        binder.forField(regionField)
                .asRequired("Region is required")
                .bind(Transaction::getRegion, Transaction::setRegion);

        binder.forField(dateField)
                .asRequired("Transaction date is required")
                .bind(Transaction::getDate, Transaction::setDate);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(amountField, ipField, numberField, regionField, dateField);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        saveButton.addClickListener(event -> saveTransaction());
        cancelButton.addClickListener(event -> clearForm());
        randomButton.addClickListener(event -> createRandomData());
        return new HorizontalLayout(saveButton, cancelButton, randomButton);
    }

    private void saveTransaction() {
        Transaction transaction = new Transaction();

        try {
            binder.writeBean(transaction);
            PostTransactionFeedback response = transactionService.postTransaction(transaction);
            String responseText = "Transaction saved successfully! (Response: %s.".formatted(response.result()) +
                    (response.result() == TransactionStatus.ALLOWED ? ")" : " Reason: %s)".formatted(response.info()));
            Notification.show(responseText, 3000, Notification.Position.MIDDLE);
            clearForm();
        } catch (ValidationException e) {
            Notification.show("Please fix the errors and try again.", 3000, Notification.Position.MIDDLE);
        }
    }

    private void clearForm() {
        binder.readBean(null);
    }

    private void createRandomData() {
        Random random = new Random();

        long amount = random.nextInt(2250) + 1;  // Generate a positive amount
        amountField.setValue(String.valueOf(amount));

        String ip = generateRandomIp();
        ipField.setValue(ip);

        String number = generateRandomCardNumber();
        numberField.setValue(number);

        Region region = Region.values()[random.nextInt(Region.values().length)];
        regionField.setValue(region);

        LocalDateTime date = generateRandomDate();
        dateField.setValue(date);
    }

    private String generateRandomIp() {
        Random random = new Random();
        return random.nextInt(256) + "." + random.nextInt(256) + "." +
                random.nextInt(256) + "." + random.nextInt(256);
    }

    private String generateRandomCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));  // Generate 16-digit random number
        }
        String number = sb.toString();
        return luhnCheck(number) ? number : generateRandomCardNumber();  // Ensure Luhn validity
    }

    private LocalDateTime generateRandomDate() {
        Random random = new Random();
        // more or less a random date/time in the last 6 years
        return LocalDateTime.ofInstant(Instant.now().minusSeconds(random.nextLong(200_000_000L))
                , ZoneId.systemDefault());
    }
}
