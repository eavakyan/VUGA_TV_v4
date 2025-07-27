<link href="{{ asset('assets/css/bootstrap.min.css') }}" rel="stylesheet">
<link href="{{ asset('assets/css/style.css') }}" rel="stylesheet">
<style>
    .padding-0 p {
        margin: 0 !important;
    }
</style>

<div class="container-fluid py-2 padding-0">
    <?php 
        echo $data;
    ?>
</div>