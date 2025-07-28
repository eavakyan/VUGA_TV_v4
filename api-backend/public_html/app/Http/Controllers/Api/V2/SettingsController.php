<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\GlobalSetting;
use App\Models\V2\CmsPage;
use App\Models\V2\AdmobConfig;
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
        
        if (!$settings) {
            return response()->json([
                'status' => false,
                'message' => 'Settings not found'
            ], 404);
        }
        
        return response()->json([
            'status' => true,
            'message' => 'App settings fetched successfully',
            'data' => [
                'settings' => $settings,
                'admob_config' => $admobConfig
            ]
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