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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A general purpose "file picker" to web storage (local storage).
 * Allows to pick existing, existing or new files and save arbitrary
 * binary data to the web storage.
 * <p>
 *     The component uses key "files" to maintain the list of used
 *     "file names". The "files" are then just mappings of those
 *     "file names". If the file contents is binary data, it is filtered
 *     through Gzip and Base64 to be stored to web storage.
 * </p>
 */
public class WebStorageFilePicker {

    /**
     * Lets the user pick an existing "file" from Web Storage or type in a name for a new one.
     * Newly created names are stored in local storage as well, so they will be available
     * if using this helper class again.
     *
     * @return the name of the "file" in web storage as {@link CompletableFuture}. Available when user has picked
     * existing or defined a new name.
     */
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

    /**
     * Shows a dialog to pick existing "file" stored in the web storage.
     * Use this to implement "File->Open" to your web app.
     *
     * @return the filename as {@link CompletableFuture} available when user has selected it.
     */
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

    public static void write(String fileName, Consumer<OutputStream> writeTask) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            GZIPOutputStream out = new GZIPOutputStream(bout);
            writeTask.accept(out);
            // WebStorage is string-string, base64 encode to String
            String data = Base64.getEncoder().encodeToString(bout.toByteArray());
            // Save to WebStorage and notify user
            WebStorage.setItem(fileName, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Picks a new or existing "file" from the web storage and writes data
     * to it.
     *
     * @param writeTask the task that writes the data once user has picked the
     *                  file.
     */
    public static void writeToNewOrExistingFile(Consumer<OutputStream> writeTask) {
        pickNewOrExistingFile().thenAccept(filename -> {
            write(filename, writeTask);
        });
    }

    /**
     * Let's the user pick an existing file whose content is then passed
     * for given callback.
     *
     * @param readTask the task to handle the file contents after user has
     *                 picked and existing file.
     */
    public static void readFromExistingFile(Consumer<InputStream> readTask) {
        WebStorageFilePicker.pickExisting().thenAccept(filename -> {
            WebStorage.getItem(filename, data -> {
                try {
                    var input = new GZIPInputStream(
                            new ByteArrayInputStream(
                                    Base64.getDecoder().decode(data)));
                    readTask.accept(input);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }


}
