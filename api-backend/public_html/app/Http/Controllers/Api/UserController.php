<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Http\Controllers\Api\V2\ProfileController;

class UserController extends Controller
{
    /**
     * Forward updateProfile to V2 ProfileController
     * This is for backward compatibility with older app versions
     */
    public function updateProfile(Request $request)
    {
        $profileController = new ProfileController();
        return $profileController->updateProfile($request);
    }
}