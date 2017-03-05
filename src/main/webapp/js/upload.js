jQuery(document).ready(function($) {

	$("#dialog-form #check-effect").change(function() {
		checkEffect($("#check-effect").val());
	});

	$("#picture-pro-dialog-form").dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: {
			"提交": function() {
				var effectType = $("#check-effect").val();
				var streamId = $("#id-list input").val();
				var srcPath = $("#src-path-list input").val();
				var dstPath = $("#dst-path-list input").val();
				//				alert("effectType: "+effectType+", streamId: "+streamId+", srcPath: "+srcPath+", dstPath: "+dstPath);

				$.ajax({
					type: "POST",
					url: "./api/v1/file/processPicture",
					dataType: "json",
					contentType: "application/json",
					data: JSON.stringify({
						"code": -1,
						"addr": srcPath,
						"rtmpAddr": dstPath,
						"streamId": streamId,
						"effectType": effectType
					}),
					async: false,
					success: function(data, textStatus, jqXHR) {
						if(data.code == 0) {

						} else if(data.code == -1) {
							alert("添加失败：该处理标识已存在！请重新添加！");
						} else {
							alert("Error code: " + data.code);
						}
					},
					error: function(data, textStatus, jqXHR) {
						alert("error:" + textStatus);
					}
				});

				$(this).dialog("close");
			},
			Cancel: function() {
				$(this).dialog("close");
			}
		},
		close: function() {
			// allFields.val("").removeClass("ui-state-error");
		}
	});

	$("#process-picture").click(function() {
			$("#picture-pro-dialog-form").dialog("open");
		}

	);
});