package com.example.application.views.main;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
        GridMenuItem<String> item = contextMenu.addItem("Download");
        DownloadTarget.getOrCreate(item).setResource(new StreamResource("Test.jpg", () -> {
            try {
                return new FileInputStream(FILE_URL);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
        add(btnDownload, grid);
    }

}
