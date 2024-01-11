package org.example.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WebStorageFilePicker {

    public static CompletableFuture<String> pickNewOrExistingFile() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        WebStorage.getItem("files", filesFromStorage -> {
            List<String> files;
            if (filesFromStorage == null) {
                files = new ArrayList<>();
            } else {
                files = new ArrayList<>(Arrays.asList(filesFromStorage.split(";")));
            }
            Select<String> existingFiles = new Select<>();
            existingFiles.setItems(files);
            existingFiles.setLabel("Pick existing");
            existingFiles.addValueChangeListener(e -> {
                        completableFuture.complete(e.getValue());
                        e.getSource().findAncestor(Dialog.class).close();
                    }
            );

            TextField typeNewName = new TextField();
            typeNewName.setPattern("[A-Za-z0-9]+");
            typeNewName.setMinLength(1);
            typeNewName.setValueChangeMode(ValueChangeMode.LAZY);
            Button save = new VButton(VaadinIcon.ARROW_RIGHT.create(), e-> {
                files.add(typeNewName.getValue());
                WebStorage.setItem("files", String.join(";", files));
                e.getSource().findAncestor(Dialog.class).close();
                completableFuture.complete(typeNewName.getValue());
            });
            save.setEnabled(false);
            typeNewName.addValueChangeListener(e -> save.setEnabled(!typeNewName.isInvalid()));

            Dialog dialog = new Dialog();
            dialog.add(new VerticalLayout(
                        new H2("Replace old"),
                        existingFiles,
                        new H2("or type in new file name..."),
                        new VHorizontalLayout(typeNewName, save).alignAll(FlexComponent.Alignment.CENTER)
                    )
            );
            dialog.open();
        });

        return completableFuture;
    }

    public static CompletableFuture<String> pickExisting() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        WebStorage.getItem("files", filesFromStorage -> {
            List<String> files;
            if (filesFromStorage == null) {
                files = Collections.emptyList();
            } else {
                files = Arrays.asList(filesFromStorage.split(";"));
            }
            Select<String> existingFiles = new Select<>();
            existingFiles.setItems(files);
            existingFiles.addValueChangeListener(e -> {
                        completableFuture.complete(e.getValue());
                        e.getSource().findAncestor(Dialog.class).close();
                    }
            );
            Dialog dialog = new Dialog();
            dialog.add(new VerticalLayout(
                            new H2("Open existing file:"),
                            existingFiles
                    )
            );
            dialog.open();

        });

        return completableFuture;
    }

}
