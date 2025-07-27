<?php

namespace App\Http\Controllers;

use App\GlobalFunction;
use App\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use Carbon\Carbon;

class TVAuthController extends Controller
{
    /**
     * Generate a new authentication session for TV login
     */
    public function generateAuthSession(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_device_id' => 'required|string|max:255'
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        try {
            // Clean up expired sessions for this device
            DB::table('tv_auth_sessions')
                ->where('tv_device_id', $request->tv_device_id)
                ->where('status', 'pending')
                ->where('expires_at', '<', Carbon::now())
                ->update(['status' => 'expired']);

            // Generate unique session token
            $sessionToken = $this->generateSecureToken();
            
            // Create QR code content (deep link URL)
            $qrCode = "vuga://auth/tv/" . $sessionToken;
            
            // Calculate expiration time (5 minutes from now)
            $expiresAt = Carbon::now()->addMinutes(5);

            // Create new session
            DB::table('tv_auth_sessions')->insert([
                'session_token' => $sessionToken,
                'qr_code' => $qrCode,
                'tv_device_id' => $request->tv_device_id,
                'status' => 'pending',
                'created_at' => Carbon::now(),
                'expires_at' => $expiresAt
            ]);

            return response()->json([
                'status' => true,
                'message' => 'Authentication session created successfully',
                'data' => [
                    'session_token' => $sessionToken,
                    'qr_code' => $qrCode,
                    'expires_at' => $expiresAt->toIso8601String(),
                    'expires_in_seconds' => 300 // 5 minutes
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to create authentication session',
                'error' => $e->getMessage()
            ]);
        }
    }

    /**
     * Check the status of an authentication session
     */
    public function checkAuthStatus(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string'
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        try {
            $session = DB::table('tv_auth_sessions')
                ->where('session_token', $request->session_token)
                ->first();

            if (!$session) {
                return response()->json([
                    'status' => false,
                    'message' => 'Session not found'
                ]);
            }

            // Check if session is expired
            if (Carbon::parse($session->expires_at)->isPast() && $session->status == 'pending') {
                DB::table('tv_auth_sessions')
                    ->where('id', $session->id)
                    ->update(['status' => 'expired']);
                    
                return response()->json([
                    'status' => false,
                    'message' => 'Session expired',
                    'data' => [
                        'auth_status' => 'expired'
                    ]
                ]);
            }

            return response()->json([
                'status' => true,
                'message' => 'Session status retrieved',
                'data' => [
                    'auth_status' => $session->status,
                    'authenticated' => $session->status === 'authenticated'
                ]
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to check authentication status',
                'error' => $e->getMessage()
            ]);
        }
    }

    /**
     * Authenticate a session from the mobile app
     */
    public function authenticateSession(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string',
            'user_id' => 'required|integer'
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        try {
            // Verify user exists
            $user = User::find($request->user_id);
            if (!$user) {
                return response()->json([
                    'status' => false,
                    'message' => 'User not found'
                ]);
            }

            // Get session
            $session = DB::table('tv_auth_sessions')
                ->where('session_token', $request->session_token)
                ->where('status', 'pending')
                ->first();

            if (!$session) {
                return response()->json([
                    'status' => false,
                    'message' => 'Invalid or expired session'
                ]);
            }

            // Check if session is expired
            if (Carbon::parse($session->expires_at)->isPast()) {
                DB::table('tv_auth_sessions')
                    ->where('id', $session->id)
                    ->update(['status' => 'expired']);
                    
                return response()->json([
                    'status' => false,
                    'message' => 'Session expired'
                ]);
            }

            // Authenticate the session
            DB::table('tv_auth_sessions')
                ->where('id', $session->id)
                ->update([
                    'user_id' => $request->user_id,
                    'status' => 'authenticated',
                    'authenticated_at' => Carbon::now()
                ]);

            return response()->json([
                'status' => true,
                'message' => 'Session authenticated successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to authenticate session',
                'error' => $e->getMessage()
            ]);
        }
    }

    /**
     * Complete authentication and get user data
     */
    public function completeAuth(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'session_token' => 'required|string',
            'tv_device_id' => 'required|string'
        ]);

        if ($validator->fails()) {
            $messages = $validator->errors()->all();
            $msg = $messages[0];
            return response()->json(['status' => false, 'message' => $msg]);
        }

        try {
            $session = DB::table('tv_auth_sessions')
                ->where('session_token', $request->session_token)
                ->where('tv_device_id', $request->tv_device_id)
                ->where('status', 'authenticated')
                ->first();

            if (!$session || !$session->user_id) {
                return response()->json([
                    'status' => false,
                    'message' => 'Session not authenticated'
                ]);
            }

            // Get user data
            $user = User::find($session->user_id);
            if (!$user) {
                return response()->json([
                    'status' => false,
                    'message' => 'User not found'
                ]);
            }

            // Update user's device info for TV
            $user->device_type = 2; // 2 for TV (you may want to add this to the comment in DB)
            $user->device_token = $request->tv_device_id;
            $user->save();

            // Mark session as completed (optional - you can delete it instead)
            DB::table('tv_auth_sessions')
                ->where('id', $session->id)
                ->delete();

            return response()->json([
                'status' => true,
                'message' => 'Authentication completed successfully',
                'data' => $user
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to complete authentication',
                'error' => $e->getMessage()
            ]);
        }
    }

    /**
     * Generate a cryptographically secure token
     */
    private function generateSecureToken($length = 32)
    {
        return bin2hex(random_bytes($length));
    }
}