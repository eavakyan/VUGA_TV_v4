<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

class UserNotificationController extends Controller
{
    /**
     * Display the user notifications management page.
     *
     * @return \Illuminate\View\View
     */
    public function index()
    {
        return view('userNotifications');
    }
}