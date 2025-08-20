<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\GlobalSetting;
use App\Models\V2\Category;
use App\Models\V2\AppLanguage;
use App\Models\V2\AdmobConfig;
use Illuminate\Http\Request;

class SettingController extends Controller
{
    /**
     * Fetch app settings
     */
    public function fetchSettings()
    {
        $setting = GlobalSetting::first();
        $categories = Category::all();
        $languages = AppLanguage::all();
        $admob = AdmobConfig::all();

        return response()->json([
            'status' => true,
            'message' => 'Fetch Setting Successfully',
            'setting' => $setting,
            'categories' => $categories,
            'genres' => $categories, // Keep for backward compatibility
            'languages' => $languages,
            'admob' => $admob
        ]);
    }
}