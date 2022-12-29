export interface DownloadTargetElem extends HTMLElement {
	$downloadConnector?: DownloadConnector;
	downloadActive?: boolean;
}

export class DownloadConnector {

	constructor(private elem: DownloadTargetElem) {
		elem.addEventListener("click", () => this.handleClick());
	}

	private handleClick() {
		if (this.elem.downloadActive) {
			const resource = this.elem.getAttribute("resourceLink");
			if (resource)
				window.open(resource, "_blank");
		}
	}

	static initLazy(elem: DownloadTargetElem) {
		try {
			if (elem.$downloadConnector)
				return;
			elem.$downloadConnector = new DownloadConnector(elem);
		}
		catch (error: any) {
			console.error("Error in download-connector:\n" + error.message, error);
		}
	}
}

const __global = window as any;
if (!__global.Custom)
	__global.Custom = {};
__global.Custom.downloadConnector = {
	initLazy: DownloadConnector.initLazy
};
