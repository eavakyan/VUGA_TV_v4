<?php

namespace App;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Constants extends Model
{
    use HasFactory;

    const movie = 1;
    const series = 2;
    
    const unfeatured = 0;
    const featured = 1;
    
    const hideContent = 0;
    const showContent = 1;

    const moreLikeRandomListCount = 10;

    const Youtube = 1;
    const FileType = 7;
    
    const Off = 0;
    const On = 1;
    
    const CustomAdSourceTypeImage = 0;
    const CustomAdSourceTypeVideo = 1;


}
