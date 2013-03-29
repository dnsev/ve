// Title management
var default_title = "dnsev / ve";
var page_list = {
	//"": null,
	"use": null,
	"github": null,
	"about": null,
	"changes": null,
	"api": {
		"test": null
	},
};
var page_list_first_open_callback = {
	"use": function () {
		$(".UseImagePreviewSmall").each(function () {
			$(this).css("background-image", "url(" + ($(this).attr("preview_href")) + ")");
		});
	},
	"api": {
		"test": function () {
			launch_api();
		}
	},
};

// Basic functions
function is_chrome() {
	return ((navigator.userAgent + "").indexOf(" Chrome/") >= 0);
}
function is_firefox() {
	var ua = navigator.userAgent + "";
	return (ua.indexOf("Mozilla/") >= 0 && ua.indexOf("MSIE") < 0);
}

function text_to_html(str) {
	return str.replace(/&/g, "&amp;")
		.replace(/>/g, "&gt;")
		.replace(/</g, "&lt;")
		.replace(/"/g, "&quot;");
}

function change_style_display(class_names, display_prefix, on) {
	on = on ? 1 : 0;
	$("." + class_names[on]).removeClass(display_prefix + "DisplayOff").addClass(display_prefix + "DisplayOn");
	$("." + class_names[1 - on]).removeClass(display_prefix + "DisplayOn").addClass(display_prefix + "DisplayOff");
}
function change_browser_display(show_all) {
	if (!show_all && is_chrome()) {
		$(".SpecificBrowser").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
		$(".Firefox").removeClass("BrowserDisplayOn").addClass("BrowserDisplayOff");
		$(".UniversalBrowser").removeClass("BrowserDisplayOn").addClass("BrowserDisplayOff");
		$(".Chrome").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
	}
	else if (!show_all && is_firefox()) {
		$(".SpecificBrowser").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
		$(".Chrome").removeClass("BrowserDisplayOn").addClass("BrowserDisplayOff");
		$(".UniversalBrowser").removeClass("BrowserDisplayOn").addClass("BrowserDisplayOff");
		$(".Firefox").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
	}
	else {
		$(".Firefox").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
		$(".Chrome").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
		$(".SpecificBrowser").removeClass("BrowserDisplayOn").addClass("BrowserDisplayOff");
		$(".UniversalBrowser").removeClass("BrowserDisplayOff").addClass("BrowserDisplayOn");
	}
}

// Custom links
var internal_link_class_list = {
};
function activate_internal_link(link) {
	// Check if it has a class
	var cl = link.attr("link_class");
	if (!cl) return true;

	// What to do
	var fcn = internal_link_class_list[cl];
	if (!fcn) return true;

	// Run the function
	return fcn(link);
}

// Window url hash management
function WindowHash() {
	this.hash = "";
	this.page = "";
	this.vars = {};
	this.history_mode = 0;

	this.history = [];
	this.history_index = -1;
};
WindowHash.prototype = {
	constructor: WindowHash,
	on_change: function (event) {
		if (this.hash == window.location.hash) return;

		// Get the new hash
		this.hash = window.location.hash;
		if (this.hash.length > 0) this.hash = this.hash.substr(1);

		// Get the page
		var h = this.hash.split("?");
		this.page = h[0];

		// Get any variables
		this.vars = this.parse_vars(h.splice(1, h.length - 1).join("?"));

		// History update
		if (this.history_mode == 0) {
			if (this.history_index < this.history.length - 1) {
				this.history.splice(this.history_index, this.history.length - 1 - this.history_index);
			}
			this.history.push([this.hash , this.page , this.vars]);
			++this.history_index;
		}
		else {
			this.history_index += this.history_mode;
			alert(this.history[this.history_index][0] + "\n" + this.hash);
		}
	},
	goto_page: function (page, vars) {
		page = page || "";

		var i = 0;
		for (var a = 1; a < arguments.length; ++a) {
			for (var v in arguments[a]) {
				page += (i == 0 ? "?" : "&") + v + (arguments[a][v] === null ? "" : "=" + arguments[a][v]);
				++i;
			}
		}

		window.location.hash = page;
	},
	has_previous: function () {
		return (this.history_index > 0);
	},
	goto_previous: function () {
		if (this.history_index > 0) {
			this.history_mode = -1;
			window.location.hash = this.history[this.history_index - 1][0];
			this.on_change();
			this.history_mode = 0;

			return true;
		}
		return false;
	},
	has_next: function () {
		return (this.history_index < this.history.length - 1);
	},
	goto_next: function () {
		if (this.history_index < this.history.length - 1) {
			this.history_mode = 1;
			window.location.hash = this.history[this.history_index + 1][0];
			this.on_change();
			this.history_mode = 0;

			return true;
		}
		return false;
	},
	parse_vars: function (str) {
		var vars = {};
		var h = str.split("&");
		for (var i = 0; i < h.length; ++i) {
			if (h[i].length == 0) continue;
			var p = h[i].split("=");
			vars[p[0]] = (p.length == 1) ? null : p.splice(1, p.length - 1).join("=");
		}

		return vars;
	},
	modify_href: function (href) {
		if (href == ".") href = this.page;
		else if (href == "..") {
			href = this.page.split("/");
			href = href.slice(0, href.length - 1).join("/");
		}
		return href;
		// TODO
	}
};
var window_hash = new WindowHash();

// Pages
function maintain_vars(vars, maintain) {
	var v = {};

	for (var k in vars) {
		for (var i = 0; i < maintain.length; ++i) {
			if (maintain[i] == k) {
				v[k] = vars[k];
				break;
			}
		}
	}

	return v;
}
function remove_vars(vars, remove) {
	var v = {};

	for (var k in vars) {
		for (var i = 0; i < remove.length; ++i) {
			if (remove[i] == k) {
				k = null;
				break;
			}
		}
		if (k !== null) v[k] = vars[k];
	}

	return v;
}
function PageBrowser() {

}
PageBrowser.prototype = {
	constructor: PageBrowser,
	open: function (page, vars, refresh) {
		// Which page
		var title = "";
		var p = page.split("/");
		var s = page_list;
		var nav_page = page;
		for (var i = 0; i < p.length; ++i) {
			if (s !== null && p[i] in s) {
				s = s[p[i]];
				title += (title.length == 0 ? "" : " / ") + p[i];
				if (i == 0) nav_page = p[i];
			}
			else {
				title = "Videncode";
				nav_page = page = "";
			}
		}
		// Callbacks
		s = page_list_first_open_callback;
		for (var i = 0; i < p.length; ++i) {
			if (s !== null && p[i] in s) {
				if (i == p.length - 1) {
					if (s[p[i]] != null && typeof(s[p[i]]) === "function") {
						s[p[i]]();
						s[p[i]] = null;
					}
					break;
				}
				s = s[p[i]];
			}
			else {
				break;
			}
		}

		$(".Content").removeClass("ContentActive");
		$(".NavigationLink").removeClass("NavigationLinkCurrent");
		$("#content_" + page.replace(/\W/g, "_")).addClass("ContentActive");
		$("#navigation_" + nav_page).addClass("NavigationLinkCurrent");

		$("title").html(default_title + (title.length == 0 ? "" : " / " + title));
		change_browser_display(true);

		$(".PageVariableDisplay").each(function () {
			$(this)
			.removeClass("PageVariableDisplayOn PageVariableDisplayOff")
			.addClass("PageVariableDisplay" + (($(this).attr("pvar") in vars) ? "Off" : "On"));
		});
		$(".PageVariableDisplayInv").each(function () {
			$(this)
			.removeClass("PageVariableDisplayOn PageVariableDisplayOff")
			.addClass("PageVariableDisplay" + (($(this).attr("pvar") in vars) ? "On" : "Off"));
		});

		image_preview_close();

		// Scroll
		var scrolled = false;
		if ("scroll" in vars) {
			var scroll_to = $("[multi_id=" + vars["scroll"].replace(/\W/g, "\\$&") + "]:visible");
			if (scroll_to.length > 0) {
				try {
					$(document).scrollTop(scroll_to.offset().top);
					scrolled = true;
				}
				catch (e) {}
			}
		}
		//if (!refresh && !scrolled) $(document).scrollTop(0);

		// Highlight
		$(".Highlighted").removeClass("Highlighted");
		if ("highlight" in vars) {
			var scroll_to = $("[multi_id=" + vars["highlight"].replace(/\W/g, "\\$&") + "]:visible");
			if (scroll_to.length > 0) {
				scroll_to.addClass("Highlighted");
			}
		}
		
		// Activate
		if ("activate" in vars) {
			var activate = $("[multi_id=" + vars["activate"].replace(/\W/g, "\\$&") + "]:visible");
			if (activate.length > 0) {
				$(activate[0]).trigger("click");
			}
		}
	}
};
var page_browser = new PageBrowser();
function maintain(extra) {
	var s_type = typeof("");
	var a_type = typeof([]);

	var r = page_vars_maintain;
	for (var i = 0; i < arguments.length; ++i) {
		if (typeof(arguments[i]) == s_type) r = r.concat(arguments[i]).split(",");
		else if (typeof(arguments[i]) == a_type) r = r.concat(arguments[i]);
	}

	return r;
}
var page_vars_maintain = [];

// Change log
var change_log_version = null;
function get_change_log() {
	var log_url = "changelog.txt";

	$.ajax({
		type: "GET",
		url: log_url,
		dataType: "text",
		success: function (data, status, jqXHR) {
			display_change_log(parse_change_log(data));
		},
		error: function (jqXHR, status, error) {
			$("#change_log").css("display", "");
		}
	});
}
function parse_change_log(data) {
	// Parse change log
	data = data.replace(/\r\n/g, "\n").split("\n\n");
	var log = [];
	for (var i = 0; i < data.length; ++i) {
		data[i] = data[i].trim();
		if (data[i].length == 0) continue;

		log.push([]);
		data[i] = data[i].split("\n");
		for (var j = 0; j < data[i].length; ++j) {
			if (j == 0) {
				log[log.length - 1].push(data[i][j]);
			}
			else {
				if (data[i][j][0] == "-") {
					log[log.length - 1].push(data[i][j].substr(1).trim());
				}
				else {
					log[log.length - 1][log[log.length - 1].length - 1] += "\n" + (data[i][j].substr(1).trim());
				}
			}
		}
	}

	return log;
}
function display_change_log(log) {
	// Output version
	change_log_version = log[0][0];
	$(".Version").html(text_to_html(change_log_version));

	// Output changelog
	var cl = $("#change_log");
	cl.css("display", "");
	cl.html("");
	for (var i = 0; i < log.length; ++i) {
		var list;
		cl.append(
			$(document.createElement("div"))
			.addClass("ChangeLogLabel")
			.html(text_to_html(log[i][0]) + (i == 0 ? "<span class=\"ChangeLogLabelCurrent\"> (current)</span>" : ""))
		);
		cl.append(
			(list = $(document.createElement("ul")))
			.addClass("ChangeLogList")
		);
		for (var j = 1; j < log[i].length; ++j) {
			list.append(
				$(document.createElement("li"))
				.html(text_to_html(log[i][j]).replace("\n", "<br />"))
			);
		}
	}
}

// Image previewing
function image_preview(obj) {
	// Only open if necessary
	if ($(".ImagePreviewBoxInner2").length > 0) {
		return;
	}

	var descr = (obj.next().length > 0 ? (obj.next().hasClass && obj.next().hasClass("ImageDescription") ? obj.next().html() : "") : "");
	var descr_container, img_append, offset, offset2;

	// Create new
	$("body").append(
		(offset = $(document.createElement("div")))
		.addClass("ImagePreviewBoxInner2")
		.append(
			(offset2 = $(document.createElement("div")))
			.append(
				(img_append = $(document.createElement("a")))
				.addClass("ImagePreviewImageContainer")
				.attr("href", obj.attr("href"))
				.attr("target", "_blank")
				.on("click", function (event) {
					return false;
				})
			)
			.append(
				(descr_container = $(document.createElement("div")))
				.addClass("ImagePreviewDescriptionContainer")
				.html(descr)
			)
		)
		.on("click", {}, function (event) {
			if (event.which == 1) {
				return false;
			}
			return true;
		})
		.css({"left": "0", "top": "0", "opacity": "0"})
	);

	// Click to close
	$(".ImagePreviewOverlay")
	.on("click", {href: "#" + window_hash.page}, function (event) {
		if (event.which == 1) {
			image_preview_close();
			// Change URL
			window_hash.goto_page(
				event.data.href,
				remove_vars(window_hash.vars, ["activate", "scroll"])
			);
			return false;
		}
		return true;
	});

	// Image
	img_append.append(
		$(document.createElement("img"))
		.attr("src", obj.attr("href"))
		.on("load", {}, function (event) {
			// Image loaded; open
			descr_container.css({
				"max-width": Math.max(640, this.width) + "px"
			});
			var w = descr_container.outerWidth();
			descr_container.css({
				"width": w,
				"max-width": ""
			});
			offset.css({
				"left": (-offset.outerWidth() / 2) + "px",
				"top": (-offset.outerHeight() / 2) + "px",
			});
			$(".ImagePreviewOverlayInner").html(
				$(document.createElement("div"))
				.addClass("ImagePreviewBox")
				.append(
					$(document.createElement("div"))
					.addClass("ImagePreviewBoxInner1")
					.append(
						offset
						.css("opacity", "")
					)
				)
			);
			$(".ImagePreviewOverlay").css("display", "block");
		})
	);
}
function image_preview_close() {
	$(".ImagePreviewBoxInner2").remove();
	$(".ImagePreviewOverlay")
	.off("click")
	.css("display", "");
}

// API test
function launch_api() {
	// Load script
	if (!(window.location.protocol == "file:" && ((navigator.userAgent + "").indexOf(" Chrome/") >= 0))) {
		$($("head")[0])
		.append(
			$(document.createElement("script"))
			.attr("src", "api_test.js")
		);
	}
}

// Entry
$(document).ready(function () {
	// Events
	$(".Link").on("click", {}, function (event) {
		if (event.which == 1) {
			event.stopPropagation();
			return true;
		}
		return true;
	});
	$(".NavigationLink").on("click", {}, function (event) {
		if (event.which == 1) {
			window_hash.goto_page(
				$(this).attr("href")[0] == "#" ? $(this).attr("href").substr(1) : $(this).attr("id").substr("navigation_".length),
				maintain_vars(window_hash.vars, maintain($(this).attr("maintain")))
			);
			return false;
		}
		return true;
	});
	$(".ImageLink").on("click", {}, function (event) {
		if (event.which == 1 || event.which === undefined) {
			var href = $(this).attr("href_update").substr(1).split("?");
			window_hash.goto_page(
				window_hash.modify_href(href[0]),
				maintain_vars(window_hash.vars, maintain($(this).attr("maintain"))),
				(href[1] ? window_hash.parse_vars(href[1]) : undefined)
			);

			image_preview($(this));
			return false;
		}
		return true;
	});
	$(".InternalLink").on("click", {}, function (event) {
		if ((event.which == 1 || event.which === undefined) && $(this).attr("href")[0] == "#") {
			var href = $(this).attr("href").substr(1).split("?");
			window_hash.goto_page(
				window_hash.modify_href(href[0]),
				maintain_vars(window_hash.vars, maintain($(this).attr("maintain"))),
				(href[1] ? window_hash.parse_vars(href[1]) : undefined)
			);
			return false;
		}
		return true;
	});
	$(".SubTitle").on("click", {}, function (event) {
		if (event.which == 1 || event.which === undefined) {
			var elem = $($(".SubTitleSegment")[1]);

			if ($(".SubTitleEmphasisToggle.SubTitleEmphasisOff").length > 0) {
				elem
				.addClass("SubTitleSegmentHidable")
				.stop(true)
				.animate({"opacity": "0"}, {duration: 200, done: function () {
					$(this).css("opacity", "0");
				}})
				.animate({"width": "0px"}, {duration: 300, done: function () {
					$(this).addClass("SubTitleSegmentHidden");
					$(".SubTitleEmphasisToggle").removeClass("SubTitleEmphasisOff");
				}});
			}
			else {
				elem.removeClass("SubTitleSegmentHidden SubTitleSegmentHidable").css("width", "auto");
				width = elem.width();
				elem.addClass("SubTitleSegmentHidable").css("width", "0px");

				elem
				.stop(true)
				.animate({"width": width}, {duration: 300, done: function () {
					$(this).removeClass("SubTitleSegmentHidable").css("width", "");
				}})
				.animate({"opacity": "1"}, {duration: 200, done: function () {
					$(this).css("opacity", "");
					$(".SubTitleEmphasisToggle").addClass("SubTitleEmphasisOff");
				}});
			}
			return false;
		}
		return true;
	});
	$(".Version").on("click", {}, function (event) {
		if ((event.which == 1 || event.which === undefined)) {
			var href = ("#changes").substr(1).split("?");
			window_hash.goto_page(
				window_hash.modify_href(href[0]),
				maintain_vars(window_hash.vars, maintain($(this).attr("maintain"))),
				(href[1] ? window_hash.parse_vars(href[1]) : undefined)
			);
			return false;
		}
		return true;
	});

	// Change log
	get_change_log();

	// Page display
	var hashchange = function (event) {
		window_hash.on_change(event);
		page_browser.open(window_hash.page, window_hash.vars, event===null);
	};
	$(window).on("hashchange", {}, hashchange);
	hashchange(null);
});


