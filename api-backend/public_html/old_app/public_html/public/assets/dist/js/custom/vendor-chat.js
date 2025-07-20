
$(document).ready(function () {
    $('.conversation-patient-1').first().trigger('click');
    window.setTimeout(function () {
        //                $('#conversation').animate({
        //                    scrollTop: $('#conversation')[0].scrollHeight}, 2000);


        $(".add").on('click', function () {
            // to make the clone removable, set clone(true)
            var newRow = $("<tr>");
            var cols = "";

            cols += '<td><select class="form-control" id="medicine_id' + counter + '" name="medicine_id[]" required="" onchange="getMedicine(' + counter + ')">\n\
                      \n\<option value="">-- Select Medicine --</option>';
            var i;
            for (i = 0; i < medicine.length; i++) {
                cols += '<option value="' + medicine[i].medicine_id + '">' + medicine[i].medicine_name + '</option>';
            }
            cols += '</select></td>';
            cols += '<td>\n\
                    <input type="hidden" id="oneprice' + counter + '" name="oneprice[]"/>\n\
                    \n\<input type="number" required="" id="qty' + counter + '" name="qty[]" min="1" value="1" onkeyup="pricecount(' + counter + ')" onchange="pricecount(' + counter + ')"  class="form-control" >\n\
                    </td>';
            cols += '<td>\n\
                    <input type="text" class="form-control"  id="price' + counter + '" name="price[]"></td>';

            cols += '<td>\n\
                    <a class="delete btn btn-sm btn-default">\n\
                    <i class="fa fa-close"></i></a></td>';
            newRow.append(cols);
            $("table.medicine-bill").append(newRow);
            counter++;
        });

        $("table.medicine-bill").on("click", ".delete", function (event) {
            $(this).closest("tr").remove();
            counter -= 1;
        });



    }, 500);
});


$(document).on("click", ".BtnAccept", function () {
    var cr_id_bill = $(this).data('id');
    $(".modal-body #cr_id_bill").val(cr_id_bill);
});

// get Price count
function pricecount(cnt) {

    var qtyMax = $('#qty' + cnt + '').prop('max');
    var Currentqty = $('#qty' + cnt + '').val();
    console.log("qtyMax >> " + qtyMax + " Currentqty >> " + Currentqty);
    if (qtyMax < Currentqty) {
        $('#success').show();
        $('#success').html('');
        $('#success').html('Available Quantity is : ' + qtyMax);
//        alert();
    }
    else {
        $('#success').html('');
        $('#success').hide();
    }
    var price = $('#qty' + cnt + '').val() * $('#oneprice' + cnt + '').val();
    $('#price' + cnt + '').val(price);
}

// Generate Bill()
function generateBill() {
    var c_id = $('#c_id').val();
    var pid = $('#user_two').val();
    //  $('#generateBill').validate({
    //   rules: {
    //   "medicine_id[]": {
    //    required: true
    //    },
    //  },
    // messages: {
    //  "medicine_id[]": {
    //  required: "Please Select Medicine"
    //   }
    // },
    // submitHandler: function (form) {
    $.ajax({
        url: BASE_URL + "Chat_controller/generateBill",
        type: "POST",
        data: $('#generateBill').serialize(),
        success: function (data) {
            console.log("data >> ", data);
            if ($.trim(data) == 1) {
                $('div#myModal').modal('hide');
                $("input[type=number]").val(1);
                $("input[type=text], select").val('');
                $(".medicine-bill").find("tr:gt(1)").remove();
                getMessageHistory(c_id, pid);
                // $(this).closest('form').find("input[type=text], select").val("");
                // $('#generateBill')[0].reset();
            }
        }
    });
    //}
    //});
}

// Get Medicine
function getMedicine(cnt) {
    console.log("cnt >> " + cnt);

    var medicine_id = $('#medicine_id' + cnt + ' option:selected').val();
    $.ajax({
        url: BASE_URL + "Chat_controller/getMedicine",
        type: "POST",
        data: {
            medicine_id: medicine_id
        },
        success: function (data) {
            //console.log("data >> ", data);
            var response = JSON.parse(data);
            if (response) {
                $('#qty' + cnt + '').prop('max', response['medicine_qty']);

                var price = $('#qty' + cnt + '').val() * response['medicine_price'];
                $('#oneprice' + cnt + '').val(response['medicine_price']);
                $('#price' + cnt + '').val(price);
            }
        }
    });
}

// Get message History
function getMessageHistory(id, pid) {
    $.ajax({
        url: BASE_URL + "Chat_controller/Message_history",
        type: "POST",
        data: {
            c_id: id,
            patient_id: pid
        },
        success: function (data) {
            //                    console.log("data >> ", data);
            $('#conversation-message').html('');
            $('#conversation-message').html(data);
        }
    });
}

// Send Message
function SendMessage() {
    var c_id = $('#c_id').val();
    var pid = $('#user_two').val();
    var comment = $('#comment').val();
    var message_img = $('#message_img').val();
    var form_data = new FormData($('#sendmessageForm')[0]);
    //  console.log(form_data);
    // var form_data = $('#sendmessageForm').serialize();


    // if ($.trim(comment) == "" || $.trim(message_img) == "")
    // {
    // }
    // else
    // {
    $.ajax({
        url: BASE_URL + "Chat_controller/SendMessage",
        type: "POST",
        enctype: 'multipart/form-data',
        cache: false,
        contentType: false,
        processData: false,
        data: form_data,
        success: function (data) {
            console.log("data >> ", data);
            if ($.trim(data) == 1) {
                getMessageHistory(c_id, pid);
                $('#conversation').animate({
                    scrollTop: $('#conversation')[0].scrollHeight
                }, 2000);
            }
        }
    });
    //}
}

// Reject Precription
function Rejectprescription() {
    var c_id = $('#c_id').val();
    var pid = $('#user_two').val();
    var cr_id = $('#Reject').data("id");

    $.ajax({
        url: BASE_URL + "Chat_controller/Rejectprescription",
        type: "POST",
        data: {
            cr_id: cr_id,
        },
        success: function (data) {
            console.log("data >> ", data);
            if ($.trim(data) == 1) {
                getMessageHistory(c_id, pid);
                $('#conversation').animate({
                    scrollTop: $('#conversation')[0].scrollHeight
                }, 2000);
            }
        }
    });
}