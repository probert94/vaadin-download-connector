export interface DownloadTargetElem extends HTMLElement {
	$downloadConnector?: DownloadConnector;
	downloadActive?: boolean;
	stopClickEventPropagation?: boolean,
	checkDownloadOnServer: boolean;
}

export class DownloadConnector {

	private resolveCanDownload?: (ok: boolean) => void;

	constructor(private elem: DownloadTargetElem) {
		elem.addEventListener("click", e => this.handleClick(e));
	}

	private async handleClick(e: MouseEvent) {
		if (this.elem.downloadActive) {
			if (this.elem.stopClickEventPropagation)
				e.stopImmediatePropagation();
			const resource = this.elem.getAttribute("resourceLink");
			if (resource) {
				const ok = !this.elem.checkDownloadOnServer || await this.checkCanDownload();
				if (ok)
					window.open(resource, "_blank");
			}
		}
	}

	private checkCanDownload(): Promise<boolean> {
		this.elem.dispatchEvent(new CustomEvent("can-download-request"));
		return new Promise(resolve => this.resolveCanDownload = resolve);
	}

	public canDownloadChecked(ok: boolean) {
		if (this.resolveCanDownload) {
			this.resolveCanDownload(ok);
			this.resolveCanDownload = undefined;
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
