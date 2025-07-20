<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class CheckHeader
{
    /**
     * Handle an incoming request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Closure(\Illuminate\Http\Request): (\Illuminate\Http\Response|\Illuminate\Http\RedirectResponse)  $next
     * @return \Illuminate\Http\Response|\Illuminate\Http\RedirectResponse
     */
    public function handle(Request $request, Closure $next)
    {
        if (isset($_SERVER['HTTP_APIKEY'])) {

            $apikey = $_SERVER['HTTP_APIKEY'];

            if ($apikey == env('API_KEY')) {
                return $next($request);
            } else {
                return response()->json([
                    'status' => false,
                    'message' => 'Enter Right Api key',
                ]);
            }
        } else {
            return response()->json([
                'status' => false,
                'message' => 'Unauthorized Access',
            ]);
        }
    }
}
