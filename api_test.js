// Test
(function () {

	if (window.location.protocol == "file:" && ((navigator.userAgent + "").indexOf(" Chrome/") >= 0)) {
		$(document).ready(function () { api_setup(); });
	}
	else {
		// Load API
		var api_load_state = 0;
		$.getScript("ve_api.js", function () {
			if (++api_load_state == 2) api_setup();
		});

		// Load test CSS
		$.ajax({
			url: "api_test.css",
			dataType: "text",
			success: function (data) {
				$($("head")[0])
				.append(
					$(document.createElement("style"))
					.html(data)
				);
				if (++api_load_state == 2) api_setup();
			}
		});
	}

	// Element creation functions
	function E(elem) {
		return $(document.createElement(elem));
	}
	function D() {
		return $(document.createElement("div"));
	}
	function text_to_html(str) {
		return str.replace(/&/g, "&amp;")
			.replace(/>/g, "&gt;")
			.replace(/</g, "&lt;")
			.replace(/"/g, "&quot;");
	}
	function decode_utf8(s) {
		return decodeURIComponent(escape(s));
	}
	function encode_utf8(s) {
		return unescape(encodeURIComponent(s));
	}

	// Player, etc.
	var videncode = null;
	var videcode = null;
	var vplayer = null;

	// Elements
	var namespace = "ve_api_test";
	var dragdrop_info_display = null;
	var dragdrop_info_display_inner = null;
	var filename_display = null;
	var tag_display = null;
	var volume_display = null;

	var video_container_outer = null;
	var video_container = null;
	var video_seek_bar_container = null;
	var video_seek_bar = null
	var video_seek_label = null;
	var video_status_label = null;

	var link_display = null;
	var video_link = null;
	var audio_link = null;
	var image_link = null;

	var video_seeking = false;
	var video_seeking_paused = false;

	var retag_container = null;
	var retag_form = null;
	var retag_sync_container = null;
	var retag_video_main_container = null;
	var retag_audio_main_container = null;
	var retag_regenerate_container = null;
	var retag_regenerate_container_error = null;
	var retag_regenerating_container = null;
	var retag_download_container = null;
	var retag_download_link = null;

	// Setup
	function api_setup() {
		// Settings
		var aspect_ratio = 16.0 / 9.0;
		var container = $("#api_test_container");
		container.html("");
		container.css("font-size", "16px");

		// Drag/drop overlay
		var div, div_container;
		$("body").append( //{
			(dragdrop_info_display = D())
			.addClass("APITestDragDropInfoContainer")
			.html(
				D()
				.addClass("APITestDragDropInfoContainerInner")
				.html(
					D()
					.addClass("APITestDragDropInfoTextContainerOuter")
					.html(
						(div_container = D())
						.addClass("APITestDragDropInfoTextContainerInner")

					)
				)
				.append(
					(div = D())
					.addClass("APITestDragDropInfoText")
					.html("Drop file here")
				)
			)
		); //}

		var w = div.outerWidth();
		var h = div.outerHeight();
		div.css({
			"width": w,
			"height": h,
			"left": (w / -2) + "px",
			"top": (h / -2) + "px"
		});
		div_container.append(div);

		dragdrop_info_display.addClass("APITestDragDropInfoContainerDisabled")

		// Events
		$("body")
		.on("dragover." + namespace, on_api_dragover)
		.on("dragenter." + namespace, on_api_dragenter)
		.on("dragexit." + namespace, on_api_dragexit)
		.on("drop." + namespace, on_api_dragdrop);

		// Video region
		container.append(
			(video_container_outer = D())
			.addClass("APITestVideoRegion")
			.html(" ")
		);

		w = video_container_outer.width();

		video_container_outer
		.height(w / aspect_ratio)
		.on("click", on_api_play_pause_click)
		.on("mousedown", on_event_ignore)
		.html(
			(video_container = D())
			.addClass("APITestVideoContainer")
		);

		h = video_container_outer.height();

		video_container_outer.append(
			(dragdrop_info_display_inner = D())
			.addClass("APITestVideoRegionText")
			.html("Drop file here")
		);
		dragdrop_info_display_inner.css("margin-top", ((h - dragdrop_info_display_inner.height()) / 2.0) + "px");

		// Seek
		container.append( //{
			(video_seek_bar_container = D())
			.addClass("APITestVideoSeekRegion")
			.append(
				(video_seek_bar = D())
				.addClass("APITestVideoSeekBar")
			)
			.append(
				(video_seek_label = D())
				.addClass("APITestVideoSeekLabel")
			)
			.on("mousedown", on_api_seek_mousedown)
		); //}
		$(document)
		.on("mouseup." + namespace, on_api_seek_mouseup)
		.on("mousemove." + namespace, on_api_seek_mousemove);


		// File / controls
		container.append( //{
			D()
			.addClass("APITestVideoFilePanel")
			.append( //{
				D()
				.addClass("APITestVideoFile")
				.append(
					E("span")
					.html("&gt; ")
				)
				.append(
					(filename_display = E("span"))
					.html("no file")
				)
			) //}
			.append( //{
				D()
				.addClass("APITestVideoFileControls")
				.append(
					(volume_display = E("span"))
					.html("?%")
				)
				.append(
					E("span")
					.html(" / ")
				)
				.append(
					E("a")
					.css("cursor", "pointer")
					.on("click", on_api_vol_add_click)
					.on("mousedown", on_event_ignore)
					.html("vol+")
				)
				.append(
					E("span")
					.html(" / ")
				)
				.append(
					E("a")
					.css("cursor", "pointer")
					.on("click", on_api_vol_sub_click)
					.on("mousedown", on_event_ignore)
					.html("vol-")
				)
			) //}
			.append(
				D()
				.addClass("APITestVideoFileControlsEnd")
			)
		); //}
		container.append( //{
			D()
			.addClass("APITestVideoFilePanel")
			.append(
				D()
				.addClass("APITestVideoFile")
				.append(
					(tag_display = E("span"))
				)
				.append(
					(link_display = E("span"))
					.css("display", "none")
					.append(
						E("span")
						.html(" / ")
					)
					.append(
						(video_link = E("a"))
						.attr("target", "_blank")
						.css("cursor", "pointer")
						.html("vid")
					)
					.append(
						E("span")
						.html(" / ")
					)
					.append(
						(audio_link = E("a"))
						.attr("target", "_blank")
						.css("cursor", "pointer")
						.html("snd")
					)
					.append(
						E("span")
						.html(" / ")
					)
					.append(
						(image_link = E("a"))
						.attr("target", "_blank")
						.css("cursor", "pointer")
						.html("img")
					)
				)
			)
			.append(
				D()
				.addClass("APITestVideoFileControls")
				.append(
					(video_status_label = E("span"))
				)
			)
			.append(
				D()
				.addClass("APITestVideoFileControlsEnd")
			)
		); //}


		// Data / re-tag API
		api_setup_retag(container);
	}
	function api_setup_retag(container) {
		// Form
		container.append(
			(retag_container = E("div"))
			.css("display", "none")
			.append(D().addClass("APITestSeparatorContainerLarge"))
			.append(
				(retag_form = E("div"))
			)
		);

		// Tag
		retag_form
		.append(D().addClass("APITestSeparatorContainer").append(D().addClass("APITestSeparator")))
		.append(
			D()
			.append(
				(retag_sync_container = D())
				.css("float", "right")
				.append(
					E("input")
					.attr("type", "text")
					.attr("maxlength", "100")
					.attr("name", "sync_offset")
					.on("change", on_api_retag_sync_change)
				)
				.append(
					E("span")
					.html(" <b>Sync offset</b>")
				)
			)
			.append(
				D()
				.append(
					E("span")
					.html("<b>Tag</b> ")
				)
				.append(
					E("input")
					.attr("type", "text")
					.attr("maxlength", "100")
					.attr("name", "tag")
					.on("change", on_api_retag_tag_change)
				)
			)
			.append(D().css("clear", "both"))
		)
		.append(
			(retag_video_main_container = D())
			.append(D().addClass("APITestSeparatorContainer").append(D().addClass("APITestSeparator")))
			.append( //{
				D()
				.css("text-align", "right")
				.css("float", "right")
				.append(
					E("div")
					.html("<b>Audio After Video</b>")
				)
				.append(
					D()
					.append(
						E("label")
						.css("display", "inline-block")
						.append(" Loop")
						.append(E("input").attr("type", "radio").attr("name", "audio_after_video_style").val("1").on("change", on_api_retag_value_change))
					)
					.append(
						E("label")
						.css("display", "inline-block")
						.css("padding-left", "8px")
						.append("Don't play")
						.append(E("input").attr("type", "radio").attr("name", "audio_after_video_style").val("0").on("change", on_api_retag_value_change))
					)
				)
				.append(
					D()
					.append(
						E("label")
						.append("Fade Out")
						.append(E("input").attr("type", "checkbox").attr("name", "audio_fade_out").on("change", on_api_retag_value_change))
					)
				)
			) //}
			.append( //{
				D()
				.append(
					E("div")
					.html("<b>Audio Before Video</b>")
				)
				.append(
					D()
					.append(
						E("label")
						.css("display", "inline-block")
						.append(E("input").attr("type", "radio").attr("name", "audio_before_video_style").val("0").on("change", on_api_retag_value_change))
						.append("Don't play")
					)
					.append(
						E("label")
						.css("display", "inline-block")
						.css("padding-left", "8px")
						.append(E("input").attr("type", "radio").attr("name", "audio_before_video_style").val("1").on("change", on_api_retag_value_change))
						.append("Loop")
					)
				)
				.append(
					D()
					.append(
						E("label")
						.append(E("input").attr("type", "checkbox").attr("name", "audio_fade_in").on("change", on_api_retag_value_change))
						.append("Fade In")
					)
				)
			) //}
			.append(D().css("clear", "both"))
		)
		.append(
			(retag_audio_main_container = D())
			.append(D().addClass("APITestSeparatorContainer").append(D().addClass("APITestSeparator")))
			.append( //{
				D()
				.css("text-align", "right")
				.css("float", "right")
				.append(
					E("div")
					.html("<b>Video After Audio</b>")
				)
				.append(
					D()
					.append(
						D()
						.css("display", "inline-block")
						.append(
							E("label")
							.css("display", "block")
							.append("Loop video")
							.append(E("input").attr("type", "radio").attr("name", "video_after_audio_style").val("1").on("change", on_api_retag_value_change))
						)
						.append(
							E("label")
							.css("display", "block")
							.append("Show image")
							.append(E("input").attr("type", "radio").attr("name", "video_after_audio_style").val("3").on("change", on_api_retag_value_change))
						)
					)
					.append(
						D()
						.css("display", "inline-block")
						.css("padding-left", "8px")
						.append(
							E("label")
							.css("display", "block")
							.append("Blank")
							.append(E("input").attr("type", "radio").attr("name", "video_after_audio_style").val("0").on("change", on_api_retag_value_change))
						)
						.append(
							E("label")
							.css("display", "block")
							.append("Video frame")
							.append(E("input").attr("type", "radio").attr("name", "video_after_audio_style").val("2").on("change", on_api_retag_value_change))
						)
					)
				)
				.append(
					D()
					.append(
						E("label")
						.append("Fade Out")
						.append(E("input").attr("type", "checkbox").attr("name", "video_fade_out").on("change", on_api_retag_value_change))
					)
				)
			) //}
			.append( //{
				D()
				.append(
					E("div")
					.html("<b>Video Before Audio</b>")
				)
				.append(
					D()
					.append(
						D()
						.css("display", "inline-block")
						.append(
							E("label")
							.css("display", "block")
							.append(E("input").attr("type", "radio").attr("name", "video_before_audio_style").val("0").on("change", on_api_retag_value_change))
							.append("Blank")
						)
						.append(
							E("label")
							.css("display", "block")
							.append(E("input").attr("type", "radio").attr("name", "video_before_audio_style").val("2").on("change", on_api_retag_value_change))
							.append("Video frame")
						)
					)
					.append(
						D()
						.css("display", "inline-block")
						.css("padding-left", "8px")
						.append(
							E("label")
							.css("display", "block")
							.append(E("input").attr("type", "radio").attr("name", "video_before_audio_style").val("1").on("change", on_api_retag_value_change))
							.append("Loop video")
						)
						.append(
							E("label")
							.css("display", "block")
							.append(E("input").attr("type", "radio").attr("name", "video_before_audio_style").val("3").on("change", on_api_retag_value_change))
							.append("Show image")
						)
					)
				)
				.append(
					D()
					.append(
						E("label")
						.append(E("input").attr("type", "checkbox").attr("name", "video_fade_in").on("change", on_api_retag_value_change))
						.append("Fade In")
					)
				)
			) //}
			.append(D().css("clear", "both"))
		)
		.append(D().addClass("APITestSeparatorContainer").append(D().addClass("APITestSeparator")))
		.append(
			(retag_regenerate_container = D())
			.css("text-align", "center")
			.append(
				E("a")
				.css("cursor", "pointer")
				.html("Regenerate")
				.on("click", on_api_retag_regenerate_click)
			)
			.append(
				(retag_regenerate_container_error = E("div"))
				.css("opacity", "0.7")
				.css("font-size", "0.8em")
			)
		)
		.append(
			(retag_regenerating_container = D())
			.css("display", "none")
			.css("text-align", "center")
			.append(
				E("span")
				.html("regenerating...")
			)
		)
		.append(
			(retag_download_container = D())
			.css("display", "none")
			.css("text-align", "center")
			.append(
				(retag_download_link = E("a"))
				.css("cursor", "pointer")
				.html("Download")
			)
			.append(" / ")
			.append(
				E("a")
				.css("cursor", "pointer")
				.html("Load in player")
				.on("click", on_api_retag_load_in_player_click)
			)
			.append(
				E("div")
				.css("opacity", "0.7")
				.css("font-size", "0.8em")
				.html("Be sure to save it as the correct file type if your browser doesn't specify a file extension")
			)
		)
		.append(D().addClass("APITestSeparatorContainer").append(D().addClass("APITestSeparator")));
	}

	// Acquire data to decode
	function on_api_get_data(ui8_data, filename) {
		// Clear old
		if (vplayer != null) {
			vplayer.reset();
			vplayer = null;
		}
		if (videcode != null) {
			videcode.reset();
			videcode = null;
		}

		// New
		videcode = new Videcode(ui8_data, filename);
		if (!videcode.decode().has_error()) {
			// Clear
			if (dragdrop_info_display_inner != null) {
				dragdrop_info_display_inner.remove();
				dragdrop_info_display_inner = null;
			}
			video_container.html("");
			retag_container.css("display", "");

			// Create data
			vplayer = new VPlayer(videcode);

			// Setup html
			video_status_label.html("loading");
			filename_display.html(text_to_html(filename));
			tag_display.html("[" + text_to_html(videcode.get_tag()) + "]");
			link_display.css("display", "");
			if (vplayer.get_video_url() != null) video_link.attr("href", vplayer.get_video_url());
			else video_link.removeAttr("href");
			if (vplayer.get_audio_url() != null) audio_link.attr("href", vplayer.get_audio_url());
			else audio_link.removeAttr("href");
			if (vplayer.get_image_url() != null) image_link.attr("href", vplayer.get_image_url());
			else image_link.removeAttr("href");

			video_seek_label.html("0");
			video_seek_bar.css("width", "0%");

			retag_video_main_container.css("display", "none");
			retag_audio_main_container.css("display", "none");
			retag_regenerating_container.css("display", "none");
			setup_retag(videcode);

			// Setup player
			vplayer
			.on("load", function (data) {
				// Set container size
				var w = video_container_outer.width();
				var h = video_container_outer.height();

				if (this.get_video_size().width > 0 && this.get_video_size().height > 0) {
					var scale = w / this.get_video_size().width;
					var scale2 = h / this.get_video_size().height;
					scale = (scale < scale2 ? scale : scale2);

					var vid_width = this.get_video_size().width * scale;
					var vid_height = this.get_video_size().height * scale;

					$(this.get_container()).css({
						"width": vid_width + "px",
						"height": vid_height + "px"
					});

					video_container.css("padding-top", ((h - vid_height) / 2.0) + "px");
				}

				// Status
				video_status_label.html("loaded");
				video_seek_label.html("0");
				video_seek_bar.css("width", "0%");

				if (vplayer.has_video_and_audio()) {
					retag_video_main_container.css("display", vplayer.is_video_main() ? "" : "none");
					retag_audio_main_container.css("display", vplayer.is_video_main() ? "none" : "");
					if (videcode != null) {
						retag_form.find("[name=sync_offset]").val(vplayer.get_sync_offset());
					}
				}
			})
			.on("error", function (data) {
				video_status_label.html("error");
			})
			.on("timeupdate", function (data) {
				if (!video_seeking) {
					video_seek_label.html(data.time);
					video_seek_bar.css("width", (data.time / data.duration * 100) + "%");
				}
			})
			.on("volumechange", function (data) {
				volume_display.html(Math.round(data.volume * 100) + "%");
			})
			.on("seek", function (data) {
				video_seek_label.html(data.time);
				video_seek_bar.css("width", (data.time / data.duration * 100) + "%");
			})
			.on("play", function (data) {
				video_status_label.html("playing");
			})
			.on("pause", function (data) {
				video_status_label.html("paused");
			})
			.on("end", function (data) {
				video_status_label.html("ended");
			})
			.create_html(video_container[0]);

			// Volume
			volume_display.html(Math.round(vplayer.get_volume() * 100) + "%");
		}
		else {
			// Error
			video_status_label.html("Error: " + videcode.get_error());
			retag_container.css("display", "none");
			link_display.css("display", "none");
			filename_display.html("no file");
			tag_display.html("");

			video_seek_label.html("0");
			video_seek_bar.css("width", "0%");
		}
	}
	function setup_retag(videcode) {
		retag_form.find("[name=tag]").val(videcode.get_tag());
		retag_form.find("[name=sync_offset]").val((videcode.get_sync_offset()));
		retag_sync_container.css("display", videcode.has_video_and_audio() ? "" : "none");

		retag_form.find("[name=audio_before_video_style][value=" + videcode.get_audio_play_style(true) + "]").prop("checked", true);
		retag_form.find("[name=audio_after_video_style][value=" + videcode.get_audio_play_style(false) + "]").prop("checked", true);

		retag_form.find("[name=audio_fade_in]").prop("checked", videcode.get_audio_fade(true));
		retag_form.find("[name=audio_fade_out]").prop("checked", videcode.get_audio_fade(false));

		retag_form.find("[name=video_before_audio_style][value=" + videcode.get_video_play_style(true) + "]").prop("checked", true);
		retag_form.find("[name=video_after_audio_style][value=" + videcode.get_video_play_style(false) + "]").prop("checked", true);

		retag_form.find("[name=video_fade_in]").prop("checked", videcode.get_video_fade(true));
		retag_form.find("[name=video_fade_out]").prop("checked", videcode.get_video_fade(false));
	}

	// Ignore event
	function on_event_ignore() {
		return false;
	}

	// Drag/drop events
	function on_api_dragover(event) {
		event.originalEvent.dataTransfer.dropEffect = "move";
		return false;
	}
	function on_api_dragenter(event) {
		dragdrop_info_display.removeClass("APITestDragDropInfoContainerDisabled");
		if (dragdrop_info_display_inner != null) dragdrop_info_display_inner.addClass("APITestVideoRegionTextDisabled");
		return false;
	}
	function on_api_dragexit(event) {
		dragdrop_info_display.addClass("APITestDragDropInfoContainerDisabled");
		if (dragdrop_info_display_inner != null) dragdrop_info_display_inner.removeClass("APITestVideoRegionTextDisabled");
		return false;
	}
	function on_api_dragdrop(event) {
		// Close overlays
		dragdrop_info_display.addClass("APITestDragDropInfoContainerDisabled");
		if (dragdrop_info_display_inner != null) dragdrop_info_display_inner.removeClass("APITestVideoRegionTextDisabled");

		// Load
		if (event.originalEvent.dataTransfer.files.length > 0) {
			for (var i = 0; i < event.originalEvent.dataTransfer.files.length; ++i) {
				// Local file
				var filename = event.originalEvent.dataTransfer.files[i].name;
				var reader = new FileReader();
				// Done function
				reader.onload = function () {
					// Convert and call load function
					var ui8_data = new Uint8Array(this.result);

					// Load data
					on_api_get_data(ui8_data, filename);
				}
				// Start
				reader.readAsArrayBuffer(event.originalEvent.dataTransfer.files[i]);
				break;
			}
		}
		else {
			// not implemented
		}

		// Done
		return false;
	}

	// Play/pause
	function on_api_play_pause_click(event) {
		if (event.which == 1) {
			if (vplayer != null) {
				if (vplayer.is_paused()) vplayer.play();
				else vplayer.pause();
			}
			return false;
		}
		return true;
	}

	// Seek bar
	function on_api_seek_mousedown(event) {
		if (event.which == 1) {
			video_seeking = true;

			if (vplayer != null) {
				video_seeking_paused = vplayer.is_paused();
				vplayer.pause();
				var percent = (event.pageX - video_seek_bar_container.offset().left) / video_seek_bar_container.width();

				if (percent < 0.0) percent = 0.0;
				else if (percent > 1.0) percent = 1.0;

				vplayer.seek(percent * vplayer.get_duration());
			}

			return false;
		}
		return true;
	}
	function on_api_seek_mouseup(event) {
		if (video_seeking) {
			video_seeking = false;
			if (!video_seeking_paused && vplayer != null && vplayer.get_time() < vplayer.get_duration()) vplayer.play();
		}
		return true;
	}
	function on_api_seek_mousemove(event) {
		if (video_seeking) {
			if (vplayer != null) {
				var percent = (event.pageX - video_seek_bar_container.offset().left) / video_seek_bar_container.width();

				if (percent < 0.0) percent = 0.0;
				else if (percent > 1.0) percent = 1.0;

				vplayer.seek(percent * vplayer.get_duration());
			}
		}
		return true;
	}

	// Volume change
	function on_api_vol_add_click(event) {
		if (event.which == 1) {
			if (vplayer != null) {
				vplayer.set_volume(vplayer.get_volume() + 0.05);
			}
			return false;
		}
		return true;
	}
	function on_api_vol_sub_click(event) {
		if (event.which == 1) {
			if (vplayer != null) {
				vplayer.set_volume(vplayer.get_volume() - 0.05);
			}
			return false;
		}
		return true;
	}

	// Retag
	function on_api_retag_value_change(event) {
		retag_regenerate_container.css("display", "");
		retag_download_container.css("display", "none");
		retag_regenerating_container.css("display", "none");
		return true;
	}
	function on_api_retag_tag_change(event) {
		var tag = $(this).val();
		tag = encode_utf8(tag);

		var maxLen = parseInt($(this).attr("maxlength"));
		if (tag.length > maxLen) {
			var loop = true;
			var newTag = "";
			while (loop && maxLen >= 0) {
				loop = false;
				try {
					newTag = decode_utf8(tag.substr(0, maxLen));
				}
				catch (e) {
					--maxLen;
					loop = true;
				}
			}
			$(this).val(newTag);
		}

		return on_api_retag_value_change();
	}
	function on_api_retag_sync_change(event) {
		var time = $(this).val();

		time = retag_validate_sync_time(parseFloat(time));

		$(this).val(time.toString());

		return on_api_retag_value_change();
	}
	function on_api_retag_regenerate_click(event) {
		if (event.which == 1) {
			if (videcode == null || videcode.has_error()) return false;

			retag_regenerate_container.css("display", "none");
			retag_download_container.css("display", "none");
			retag_regenerating_container.css("display", "");

			if (videncode != null) {
				videncode.reset();
				videncode = null;
			}

			// Create new
			var sync_offset = parseFloat(retag_form.find("[name=sync_offset]").val());
			if (sync_offset != sync_offset || !isFinite(sync_offset)) sync_offset = 0.0;

			var o;
			videncode = new Videncode();
			videncode
			.set_image(videcode.get_image(), videcode.get_image_mime_type())
			.set_video(videcode.get_video())
			.set_audio(videcode.get_audio())
			.set_tag(retag_form.find("[name=tag]").val())
			.set_sync_offset(sync_offset);
			if (videcode.has_video_and_audio()) {
				if (vplayer == null || vplayer.is_video_main()) {
					videncode
					.set_audio_play_style(true, (o = retag_form.find("[name=audio_before_video_style]:checked")).length > 0 ? parseInt(o.val()) : 0)
					.set_audio_play_style(false, (o = retag_form.find("[name=audio_after_video_style]:checked")).length > 0 ? parseInt(o.val()) : 0)
					.set_audio_fade(true, retag_form.find("[name=audio_fade_in]:checked").length > 0)
					.set_audio_fade(false, retag_form.find("[name=audio_fade_out]:checked").length > 0);
				}
				if (vplayer == null || !vplayer.is_video_main()) {
					videncode
					.set_video_play_style(true, (o = retag_form.find("[name=video_before_audio_style]:checked")).length > 0 ? parseInt(o.val()) : 0)
					.set_video_play_style(false, (o = retag_form.find("[name=video_after_audio_style]:checked")).length > 0 ? parseInt(o.val()) : 0)
					.set_video_fade(true, retag_form.find("[name=video_fade_in]:checked").length > 0)
					.set_video_fade(false, retag_form.find("[name=video_fade_out]:checked").length > 0);
				}
			}

			// Encode
			if (videncode.encode().has_error()) {
				// Error
				retag_regenerate_container.css("display", "");
				retag_regenerate_container_error.html("Error: " + videncode.encode().get_error());
				retag_download_container.css("display", "none");
				retag_regenerating_container.css("display", "none");
			}
			else {
				// No error
				retag_regenerate_container.css("display", "none");
				retag_download_container.css("display", "");
				retag_regenerating_container.css("display", "none");

				// Create link
				var url = videncode.get_url();
				retag_download_link.attr("href", url);
			}

			return false;
		}
		return true;
	}
	function on_api_retag_load_in_player_click(event) {
		if (event.which == 1) {
			if (videncode != null && !videncode.has_error()) {
				on_api_get_data(
					videncode.get_data(),
					"blob" +
					(videncode.get_image_mime_type() == "image/png" ? ".png" : (
					videncode.get_image_mime_type() == "image/gif" ? ".gif" : ".jpg" ))
				);
			}

			return false;
		}
		return true;
	}

	function retag_validate_sync_time(time) {
		if (time != time || !isFinite(time)) time = 0.0;
		else if (time < 0.0) time = 0.0;
		else if (vplayer != null) {
			if (time + vplayer.get_min_duration() > vplayer.get_duration()) {
				time = vplayer.get_duration() - vplayer.get_min_duration();
			}
		}

		return time;
	}

})();

