function fillProcessTable(streamId, fileType, effectType, srcPath, dstPath) {
	var item = document.querySelector("#process-tr-template");
	item.content.querySelector("tr").id = "tr-" + streamId;
	item.content.querySelector("tr td[name='stream-id']").innerHTML = streamId;
	item.content.querySelector("tr td[name='file-type']").innerHTML = fileType;
	item.content.querySelector("tr td[name='effect-type']").innerHTML = effectType;
	item.content.querySelector("tr td[name='src-path']").innerHTML = srcPath;
	item.content.querySelector("tr td[name='dst-path']").innerHTML = dstPath;
	//	item.content.querySelector("tr td[name='report-path']").innerHTML = "reportPath";
	$("#process-tbody").append(item.content.cloneNode(true));
}
var qMap = {};
// Clock function define. Every interval invoke the queryResult func.
// return the polling func's id
function startToPollResult(streamId) {
	var qId = window.setInterval(function() {
		queryResult(streamId)
	}, 1000);
	alert("start to poll process result! " + qId);
	qMap[streamId] = qId;
}
function queryResult(streamId) {
	var mMap = {};
	mMap["streamId"] = streamId;
	$.ajax({
		type: "post",
		url: "./api/v1/file/queryProcessResult",
		async: true,
		dataType: "json",
		contentType: "application/json",
		data: JSON.stringify({
			"map": mMap
		}),
		success: function(data, textStatus, jqXHR) {
			if(data.code == 0) {
				alert("result is ready: " + data.map["repoPath"]);
				displayResult(streamId, data.map["repoPath"]);
			}
		},
		error: function(data, textStatus, jqXHR) {
			alert("结果查询失败！退出 error:" + textStatus);
			window.clearInterval(qMap[streamId]);
		}
	});
}
function displayResult(streamId, repoPath) {
	//	alert("#tr-"+streamId+" td[name='report-path']")
	$("#tr-" + streamId + " td[name='report-path']").html(repoPath);
	// $("#test-label") = repoPath;
	window.clearInterval(qMap[streamId]); // Stop the queryResult func.
}
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
				var streamId = $("#stream-id-list input").val();
				var srcPath = $("#src-path-list input").val();
				var dstPath = $("#dst-path-list input").val();
				//				alert("effectType: "+effectType+", streamId: "+streamId+", srcPath: "+srcPath+", dstPath: "+dstPath);
				var mMap = {};
				mMap["effectType"] = effectType;
				mMap["fileType"] = "图片";
				mMap["streamId"] = streamId;
				mMap["srcPath"] = srcPath;
				mMap["dstPath"] = dstPath;
				$.ajax({
					type: "POST",
					url: "./api/v1/file/processPicture",
					async: true,
					dataType: "json",
					contentType: "application/json",
					data: JSON.stringify({
						"map": mMap
					}),
					async: false,
					success: function(data, textStatus, jqXHR) {
						if(data.code == 0) {
							fillProcessTable(data.map["streamId"], data.map["fileType"],
								data.map["effectType"], data.map["srcPath"], data.map["dstPath"]);
							startToPollResult(data.map["streamId"]);
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
	$("#process-test").click(function() {
		var mMap = {};
		mMap["streamId"] = "1";
		mMap["repoPath"] = "repo";
		$.ajax({
			type: "post",
			url: "./api/v1/file/testSql",
			async: true,
			dataType: "json",
			contentType: "application/json",
			data: JSON.stringify({
				"map": mMap
			}),
			success: function(data, textStatus, jqXHR) {
				if(data.code == 0) {
					alert("test for sql");
				}
			},
			error: function(data, textStatus, jqXHR) {
				alert("ceshi失败！退出 error:" + textStatus);
			}
		});
	});
});