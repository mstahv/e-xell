package org.example.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import org.apache.commons.io.FileUtils;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.upload.UploadFileHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Route
public class MainView extends VerticalLayout {

    private final UploadFileHandler openItem;
    private final DynamicFileDownloader saveItem;
    private final Button closeItem;
    private final Button newItem;
    private final Button saveToWebStorage;
    private final Button openFromWebStorage;
    private Spreadsheet spreadsheet;

    private Component about = new RichText().withMarkDown("""
Welcome to e-xell! 

This is not a good app, don't use if for anything real. 

This is demo app. Check the [GitHub page](https://github.com/mstahv/e-xell) why this app exists.
""");


    public MainView() {
        newItem = new VButton("New...", e -> createNew())
                .withTooltip("Creates a new spreadsheet");
        openItem = new UploadFileHandler((content, fileName, mimeType) -> {
            return openExcellFile(content);
        })
                .withDragAndDrop(false)
                .withUploadButton(new VButton(VaadinIcon.UPLOAD.create())
                        .withTooltip("Upload .xslx file...")
                )
                .withAcceptedFileTypes(".xls", ".xlsx", "application/vnd.ms-excel");

        openFromWebStorage = new VButton(VaadinIcon.CLOUD_UPLOAD.create(), this::openFromWebStorage)
                .withTooltip("Open previously saved file from browsers local storage...");

        saveItem = new DynamicFileDownloader(
                new VButton(VaadinIcon.DOWNLOAD.create()),
                "saved-vaadin-e-xell-file.xlsx",
                this::writeXlsFile);

        saveToWebStorage = new VButton(VaadinIcon.CLOUD_DOWNLOAD.create(), this::saveToWebStorage)
                .withTooltip("Saves current file to browsers local storage...");

        closeItem = new Button("Close", e -> close());
        closeItem.setEnabled(true);

        Arrays.asList(saveItem, saveToWebStorage, closeItem).forEach(i -> i.setEnabled(false));
        add(new VHorizontalLayout(newItem, openItem, openFromWebStorage, saveItem, saveToWebStorage, closeItem));

        add(about);
    }

    private void close() {
        spreadsheet.removeFromParent();
        spreadsheet = null;
        add(about);
        maintainMenuItems();
    }

    private void createNew() {
        spreadsheet = new Spreadsheet();
        displaySpreadsheet();
    }

    private void displaySpreadsheet() {
        addAndExpand(spreadsheet);
        about.removeFromParent();
        maintainMenuItems();
    }

    private void maintainMenuItems() {
        boolean fileOpen = spreadsheet != null;
        Arrays.asList(saveItem, saveToWebStorage, closeItem).forEach(i -> i.setEnabled(fileOpen));
        Arrays.asList(openItem, openFromWebStorage, newItem).forEach(i -> i.setEnabled(!fileOpen));
    }

    public void writeXlsFile(OutputStream out) {
        try {
            spreadsheet.write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Command openExcellFile(InputStream content) {
        try {
            spreadsheet = new Spreadsheet(content);
            return () -> displaySpreadsheet(); // Execute later in UI thread
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToWebStorage() {
        WebStorageFilePicker.pickNewOrExistingFile().thenAccept(filename -> {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                GZIPOutputStream out = new GZIPOutputStream(bout);
                spreadsheet.write(out);
                String data = Base64.getEncoder().encodeToString(bout.toByteArray());
                WebStorage.setItem(filename, data);
                String sizeStr = FileUtils.byteCountToDisplaySize(data.length());
                Notification.show("Saved %s to your browsers local storage.".formatted(sizeStr));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void openFromWebStorage() {
        WebStorageFilePicker.pickExisting().thenAccept(filename -> {
            WebStorage.getItem(filename, data -> {
                try {
                    var input = new GZIPInputStream(
                            new ByteArrayInputStream(
                                    Base64.getDecoder().decode(data)));
                    openExcellFile(input).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        });

    }

}
