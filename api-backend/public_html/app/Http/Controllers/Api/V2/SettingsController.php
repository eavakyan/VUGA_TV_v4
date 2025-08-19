<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\GlobalSetting;
use App\Models\V2\CmsPage;
use App\Models\V2\AdmobConfig;
use App\Models\V2\Genre;
use App\Models\V2\AppLanguage;
use Illuminate\Http\Request;

class SettingsController extends Controller
{
    /**
     * Get app settings
     */
    public function getAppSettings(Request $request)
    {
        $settings = GlobalSetting::first();
        $admobConfig = AdmobConfig::all();
        $genres = Genre::orderBy('title')->get();
        $languages = AppLanguage::orderBy('title')->get();
        
        if (!$settings) {
            return response()->json([
                'status' => false,
                'message' => 'Settings not found'
            ], 404);
        }
        
        // Format response to match what iOS expects
        return response()->json([
            'status' => true,
            'message' => 'App settings fetched successfully',
            'setting' => $settings,  // iOS expects 'setting' not 'data.settings'
            'genres' => $genres,
            'languages' => $languages,
            'admob' => $admobConfig  // iOS expects 'admob' not 'admob_config'
        ]);
    }
    
    /**
     * Get CMS pages (privacy policy, terms of use)
     */
    public function getCmsPages(Request $request)
    {
        $cmsPage = CmsPage::first();
        
        if (!$cmsPage) {
            return response()->json([
                'status' => false,
                'message' => 'CMS pages not found'
            ], 404);
        }
        
        return response()->json([
            'status' => true,
            'message' => 'CMS pages fetched successfully',
            'data' => $cmsPage
        ]);
    }
}