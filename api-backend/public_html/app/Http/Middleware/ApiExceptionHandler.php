<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;
use Illuminate\Validation\ValidationException;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Symfony\Component\HttpFoundation\Response;
use App\Http\Resources\ApiResponse;
use Throwable;

class ApiExceptionHandler
{
    /**
     * Handle an incoming request.
     *
     * @param  \Closure(\Illuminate\Http\Request): (\Symfony\Component\HttpFoundation\Response)  $next
     */
    public function handle(Request $request, Closure $next): Response
    {
        try {
            return $next($request);
        } catch (ValidationException $e) {
            return ApiResponse::error(
                'Validation failed',
                422,
                $e->errors()
            );
        } catch (ModelNotFoundException $e) {
            return ApiResponse::error(
                'Resource not found',
                404
            );
        } catch (Throwable $e) {
            // Log the error for debugging
            \Log::error('API Exception: ' . $e->getMessage(), [
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'trace' => $e->getTraceAsString()
            ]);

            // Return generic error in production
            if (app()->environment('production')) {
                return ApiResponse::error(
                    'Internal server error',
                    500
                );
            }

            // Return detailed error in development
            return ApiResponse::error(
                $e->getMessage(),
                500,
                [
                    'file' => $e->getFile(),
                    'line' => $e->getLine(),
                ]
            );
        }
    }
} 