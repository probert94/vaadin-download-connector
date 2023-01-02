package com.example.application.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.shared.Registration;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@JsModule("./src/download-connector.ts")
public interface DownloadTarget<C extends Component> extends HasElement {
	String TOKEN_CAN_DOWNLOAD_REQUEST_REGISTRATION	= "CanDownloadRequestRegistration";
	String ATTR_RESOURCE_LINK						= "resourceLink";
	String PROP_ACTIVE								= "downloadActive";
	String PROP_STOP_CLICK_EVENT_PROPAGATION		= "stopClickEventPropagation";
	String PROP_CHECK_DOWNLOAD_ON_SERVER			= "checkDownloadOnServer";

	@SuppressWarnings("unchecked")
	default C getTargetComponent() {
		return (C)this;
	}

	@Override
	default Element getElement() {
		return getTargetComponent().getElement();
	}

	default void setResource(StreamResource resource) {
		boolean stopClickEventPropagation = false;
		if (resource == null)
			getElement().removeAttribute(ATTR_RESOURCE_LINK);
		else {
			if (getTargetComponent() instanceof MenuItemBase<?,?,?> mi) {
				stopClickEventPropagation = true;
				StreamResourceWriter writer = resource.getWriter();
				resource = new StreamResource(resource.getName(), (os, session) -> {
					writer.accept(os, session);
					mi.getUI().orElseThrow().access(() -> mi.getContextMenu().close());
				});
			}
			getElement().setAttribute(ATTR_RESOURCE_LINK, resource);
		}
		getElement().setProperty(PROP_STOP_CLICK_EVENT_PROPAGATION, stopClickEventPropagation);
	}

	default void setResource(String resource) {
		if (resource != null)
			getElement().setAttribute(ATTR_RESOURCE_LINK, resource);
		else
			getElement().removeAttribute(ATTR_RESOURCE_LINK);
		getElement().setProperty(PROP_STOP_CLICK_EVENT_PROPAGATION, false);
	}

	default void setCanDownloadChecker(CanDownloadChecker downloadChecker) {
		C component = getTargetComponent();
		Object reg = ComponentUtil.getData(component, TOKEN_CAN_DOWNLOAD_REQUEST_REGISTRATION);
		if (reg instanceof Registration registration)
			registration.remove();
		getElement().setProperty(PROP_CHECK_DOWNLOAD_ON_SERVER, downloadChecker != null);
		if (downloadChecker == null)
			ComponentUtil.setData(component, TOKEN_CAN_DOWNLOAD_REQUEST_REGISTRATION, null);
		else {
			Registration registration = getElement().addEventListener("can-download-request",
				e -> downloadChecker.canDownload().whenCompleteAsync(
					(r, exc) -> {
						resolveCanDownload(r);
						if (!r
							&& getElement().getProperty(PROP_STOP_CLICK_EVENT_PROPAGATION, false)
							&& getTargetComponent() instanceof MenuItemBase<?,?,?> mi)
						{
							mi.getContextMenu().close();
						}
					},
					cmd -> component.getUI().orElseThrow().access(cmd::run)
				)
			);
			ComponentUtil.setData(component, TOKEN_CAN_DOWNLOAD_REQUEST_REGISTRATION, registration);
		}
	}

	private PendingJavaScriptResult resolveCanDownload(Boolean r) {
		return getElement().executeJs("this.$downloadConnector.canDownloadChecked($0)", r != null && r);
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
