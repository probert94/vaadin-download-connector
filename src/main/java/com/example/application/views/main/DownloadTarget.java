package com.example.application.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@JsModule("./src/download-connector.ts")
public interface DownloadTarget<C extends Component> extends HasElement {
	String ATTR_RESOURCE_LINK	= "resourceLink";
	String PROP_ACTIVE			= "downloadActive";

	@SuppressWarnings("unchecked")
	default C getTargetComponent() {
		return (C)this;
	}

	@Override
	default Element getElement() {
		return getTargetComponent().getElement();
	}

	default void setResource(StreamResource resource) {
		if (resource != null)
			getElement().setAttribute(ATTR_RESOURCE_LINK, resource);
		else
			getElement().removeAttribute(ATTR_RESOURCE_LINK);
	}

	default void setResource(String resource) {
		if (resource != null)
			getElement().setAttribute(ATTR_RESOURCE_LINK, resource);
		else
			getElement().removeAttribute(ATTR_RESOURCE_LINK);
	}

	default boolean isActive() {
		return getElement().getProperty(PROP_ACTIVE, false);
	}

	default void setActive(boolean active) {
		getElement().setProperty(PROP_ACTIVE, active);
	}

	private void initializeConnector() {
		getElement().executeJs("window.Custom.downloadConnector.initLazy(this);");
	}

	/** Initialisiert das Download-Feature. */
	private void initialize() {
		Component component = getTargetComponent();
		Element element = getElement();
		Command command = this::initializeConnector;
		runWhenAttached(component, command);
		element.addDetachListener(e -> runWhenAttached(component, command));
		setActive(true);
	}

	private static void runWhenAttached(Component component, Command command) {
		component.getElement().getNode().runWhenAttached(
			ui -> ui.beforeClientResponse(component, context -> command.execute())
		);
	}

	@SuppressWarnings("unchecked")
	static <T extends Component> Optional<DownloadTarget<T>> get(T target) {
		return Optional.ofNullable(ComponentUtil.getData(target, DownloadTarget.class));
	}

	static <T extends Component> DownloadTarget<T> getOrCreate(T target) {
		return get(target).orElseGet(() -> create(target));
	}

	interface CanDownloadChecker {
		CompletionStage<Boolean> canDownload();
	}

	class DownloadEndEvent<C extends Component> extends ComponentEvent<C> {
		public DownloadEndEvent(C source, boolean fromClient) {
			super(source, fromClient);
		}
	}

	private static <T extends Component> DownloadTarget<T> create(T target) {
		DownloadTarget<T> wd = new DownloadTarget<>() {
			@Override
			public T getTargetComponent() {
				return target;
			}
		};
		ComponentUtil.setData(target, DownloadTarget.class, wd);
		wd.initialize();
		return wd;
	}
}
