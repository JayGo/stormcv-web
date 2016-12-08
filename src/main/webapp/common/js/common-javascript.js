jQuery(document).ready(function($) {

	$("#header .head").width($(window).width() * 0.625);

	// 根据menu高度自动设置列表高度 
	// 20是".left-menu .leftnav-camlist"本身的padding top值，设置其高度时，无法在右边用jquery API获取，因为与设置height()
	// 本身冲突，因此只好用常数
	$(".left-menu .leftnav-camlist .camlist-ul").height(320 / 935 * $(".left-menu").height());
	$(".left-menu .leftnav-camlist").height(320 / 935 * $(".left-menu").height() + 20);


	// 控制左侧按钮栏的弹出和收回，id=left的small类用于表示收回状态，toggleClass()用于动态添加/删除small类,从而控制按钮栏的弹出和收回
	$("#left-btn").click(function() {
		$("#left-menu").toggleClass("small");

		// 控制右侧mainbody的宽度(通过设置container的left padding值)
		if ($("#left-menu").hasClass("small"))
			$("#container").css({
				"padding-left": $(".left-menu.small").width() + $(".left-btn").width() + 5 + "px"
			});
		else
			$("#container").css({
				"padding-left": $(".left-menu").width() + $(".left-btn").width() + 5 + "px"
			});
	});



});