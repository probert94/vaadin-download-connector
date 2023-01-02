package com.example.application.views.main;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

@PageTitle("Main")
@Route(value = "")
public class MainView extends HorizontalLayout {

    private static final String FILE_URL = "C:\\Users\\User\\Pictures\\Test.jpg";

    public MainView() {
        Button btnDownload = new Button("Download");
        DownloadTarget.getOrCreate(btnDownload).setResource(new StreamResource("Test.jpg", () -> {
            try {
                return new FileInputStream(FILE_URL);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
        Grid<String> grid = new Grid<>();
        grid.setItems("1", "2", "3");
        grid.addColumn(item -> item)
            .setHeader("Item");
        GridContextMenu<String> contextMenu = grid.addContextMenu();
        contextMenu.setDynamicContentHandler(i -> {
            contextMenu.removeAll();
            GridMenuItem<String> item = contextMenu.addItem("Download");
            DownloadTarget<GridMenuItem<String>> download = DownloadTarget.getOrCreate(item);
            download.setResource(new StreamResource("Test.jpg", () -> {
                try {
                    return new FileInputStream(FILE_URL);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }));
            if (i == null) {
                download.setCanDownloadChecker(() -> {
                    var dialog = new Dialog();
                    dialog.add(new Span("Download must be executed on a item"));
                    dialog.add(new Button("OK", e -> dialog.close()));
                    dialog.setModal(true);
                    dialog.open();
                    CompletableFuture<Void> cf = new CompletableFuture<>();
                    dialog.addOpenedChangeListener(e -> cf.complete(null));
                    return cf.thenApply(r -> false);
                });
            }
            return true;
        });
        add(btnDownload, grid);
    }

}
