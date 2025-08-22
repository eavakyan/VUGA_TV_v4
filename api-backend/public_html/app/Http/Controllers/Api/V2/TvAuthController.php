<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\TvAuthSession;
use App\Models\V2\AppUser;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class TvAuthController extends Controller
{
    /**
     * Generate a new TV authentication session
     */
    public function generateSession(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_device_id' => 'required|string|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        // Generate unique session token
        $sessionToken = Str::random(64);
        
        // Generate QR code content (deep link URL matching mobile app format)
        $qrCode = "vuga://auth/tv/" . $sessionToken;

        // Create new session
        $session = new TvAuthSession;
        $session->session_token = $sessionToken;
        $session->qr_code = $qrCode;
        $session->tv_device_id = $request->tv_device_id;
        $session->status = 'pending';
        $session->expires_at = now()->addMinutes(5);
        $session->save();

        return response()->json([
            'status' => true,
            'message' => 'TV authentication session created',
            'data' => [
                'session_token' => $sessionToken,
                'qr_code' => $qrCode,
                'expires_at' => $session->expires_at,
                'expires_in_seconds' => 300 // 5 minutes
            ]
        ]);
    }

    /**
     * Authenticate a TV session with user credentials
     */
    public function authenticate(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string|exists:tv_auth_session,session_token',
            'app_user_id' => 'required|integer|exists:app_user,app_user_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $session = TvAuthSession::where('session_token', $request->session_token)
                                ->where('status', 'pending')
                                ->where('expires_at', '>', now())
                                ->first();

        if (!$session) {
            return response()->json([
                'status' => false,
                'message' => 'Session expired or already authenticated'
            ], 400);
        }

        // Authenticate the session
        $session->app_user_id = $request->app_user_id;
        $session->status = 'authenticated';
        $session->authenticated_at = now();
        $session->save();

        // Get user data
        $user = AppUser::find($request->app_user_id);

        return response()->json([
            'status' => true,
            'message' => 'TV authenticated successfully',
            'data' => [
                'session' => $session,
                'user' => $user
            ]
        ]);
    }

    /**
     * Check TV session status
     */
    public function checkStatus(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string|exists:tv_auth_session,session_token',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $session = TvAuthSession::with('user')
                                ->where('session_token', $request->session_token)
                                ->first();

        if (!$session) {
            return response()->json([
                'status' => false,
                'message' => 'Session not found'
            ], 404);
        }

        // Check if expired
        if ($session->expires_at < now() && $session->status == 'pending') {
            $session->status = 'expired';
            $session->save();
        }

        return response()->json([
            'status' => true,
            'message' => 'Session status fetched',
            'data' => [
                'session_status' => $session->status,
                'authenticated' => $session->status == 'authenticated',
                'user' => $session->status == 'authenticated' ? $session->user : null,
                'expires_at' => $session->expires_at
            ]
        ]);
    }

    /**
     * Logout TV session
     */
    public function logout(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string|exists:tv_auth_session,session_token',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $session = TvAuthSession::where('session_token', $request->session_token)
                                ->where('status', 'authenticated')
                                ->first();

        if (!$session) {
            return response()->json([
                'status' => false,
                'message' => 'Session not found or not authenticated'
            ], 400);
        }

        // Expire the session
        $session->status = 'expired';
        $session->save();

        return response()->json([
            'status' => true,
            'message' => 'TV session logged out successfully'
        ]);
    }
    
    /**
     * Authenticate session - Legacy endpoint for mobile app compatibility
     * The mobile app expects 'user_id' instead of 'app_user_id'
     */
    public function authenticateSession(Request $request)
    {
        // Map user_id to app_user_id for compatibility
        if ($request->has('user_id') && !$request->has('app_user_id')) {
            $request->merge(['app_user_id' => $request->user_id]);
        }
        
        return $this->authenticate($request);
    }
    
    /**
     * Complete authentication and get user data - Legacy endpoint
     */
    public function completeAuth(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string',
            'tv_device_id' => 'required|string'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $session = TvAuthSession::with('user')
                                ->where('session_token', $request->session_token)
                                ->where('tv_device_id', $request->tv_device_id)
                                ->where('status', 'authenticated')
                                ->first();

        if (!$session || !$session->app_user_id) {
            return response()->json([
                'status' => false,
                'message' => 'Session not authenticated'
            ], 400);
        }

        // Get user data
        $user = AppUser::find($session->app_user_id);
        if (!$user) {
            return response()->json([
                'status' => false,
                'message' => 'User not found'
            ], 404);
        }

        // Update user's device info for TV
        $user->device_type = 2; // 2 for TV
        $user->device_token = $request->tv_device_id;
        $user->save();

        // Delete the session after successful authentication
        $session->delete();

        return response()->json([
            'status' => true,
            'message' => 'Authentication completed successfully',
            'data' => $user
        ]);
    }
}