package org.example.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.RichText;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.upload.UploadFileHandler;

import java.io.IOException;
import java.util.Arrays;

@Route
public class MainView extends VerticalLayout {
    private Spreadsheet spreadsheet;

    private final Button newItem = new VButton("New...", this::createNew)
            .withTooltip("Creates a new spreadsheet");
    private final Button closeItem = new VButton("Close", this::close);
    private final UploadFileHandler openItem = new UploadFileHandler(
                (inputStream, metadata) -> {
                    spreadsheet = new Spreadsheet(inputStream);
                    return () -> displaySpreadsheet(); // Execute later in UI thread
                })
            // make browser accept only relevant files
            .withAcceptedFileTypes(".xls", ".xlsx", "application/vnd.ms-excel")
            // disable d&d, icon only -> just a compact icon in toolbar
            .withDragAndDrop(false)
            .withUploadButton(new VButton(VaadinIcon.UPLOAD.create())
                    .withTooltip("Upload .xslx file...")
            );

    private final DynamicFileDownloader saveItem = new DynamicFileDownloader(
            out -> spreadsheet.write(out)
    ).withTooltip("Downloads current file to disk...");

    private final Button saveToWebStorage = new VButton(
            VaadinIcon.CLOUD_DOWNLOAD.create(), this::saveToWebStorage)
            .withTooltip("Saves current file to browsers local storage...");
    private final Button openFromWebStorage = new VButton(
                VaadinIcon.CLOUD_UPLOAD.create(), this::openFromWebStorage)
            .withTooltip("Open previously saved file from browsers local storage...");

    private Component about = new RichText().withMarkDown("""
        # Welcome to e-xell!
        
        This is not a good app, don't use if for anything real. 
        
        This is demo app. Check the [GitHub page](https://github.com/mstahv/e-xell) why this app exists.
        """);


    public MainView() {
        // Initial state for toolbar buttons
        Arrays.asList(saveItem, saveToWebStorage, closeItem).forEach(i -> i.setEnabled(false));
        // create toolbar
        add(new VHorizontalLayout(newItem, openItem, openFromWebStorage, saveItem, saveToWebStorage, closeItem));
        // add about text/disclaimer
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

    public void saveToWebStorage() {
        WebStorageFilePicker.writeToNewOrExistingFile(outputStream -> {
            try {
                spreadsheet.write(outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Notification.show("File saved to your browsers local storage!");
    }

    public void openFromWebStorage() {
        WebStorageFilePicker.readFromExistingFile(input -> {
            try {
                spreadsheet = new Spreadsheet(input);
                displaySpreadsheet();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
