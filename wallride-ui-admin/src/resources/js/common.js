/***** pagination *****/
$(function(){
	$('.pagination .disabled a').click(function(){
		return false;
	});
/*    $('.sortable').sortable({
        helper: function(e, tr)
        {
            var $originals = tr.children();
            var $helper = tr.clone();
            $helper.children().each(function(index)
            {
                // Set helper cell sizes to match the original sizes
                $(this).width($originals.eq(index).width());
            });
            return $helper;
        }
    });*/
    const iconSize = 16;
    feather.replace({
        'width': iconSize,
        'height': iconSize
    });
});
/***** pagination *****/