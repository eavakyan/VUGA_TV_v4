<?php

namespace App\Services;

use Illuminate\Support\Facades\Cache;
use App\Genre;
use App\Setting;
use App\Language;

class CacheService
{
    const CACHE_TTL = 3600; // 1 hour
    const GENRES_CACHE_KEY = 'genres_all';
    const SETTINGS_CACHE_KEY = 'app_settings';
    const LANGUAGES_CACHE_KEY = 'languages_all';
    const FEATURED_CONTENT_KEY = 'featured_content';

    /**
     * Get all genres with caching
     */
    public function getGenres()
    {
        return Cache::remember(self::GENRES_CACHE_KEY, self::CACHE_TTL, function () {
            return Genre::orderBy('title', 'ASC')->get();
        });
    }

    /**
     * Get app settings with caching
     */
    public function getSettings()
    {
        return Cache::remember(self::SETTINGS_CACHE_KEY, self::CACHE_TTL, function () {
            return Setting::first();
        });
    }

    /**
     * Get all languages with caching
     */
    public function getLanguages()
    {
        return Cache::remember(self::LANGUAGES_CACHE_KEY, self::CACHE_TTL, function () {
            return Language::orderBy('title', 'ASC')->get();
        });
    }

    /**
     * Clear genre cache
     */
    public function clearGenresCache(): void
    {
        Cache::forget(self::GENRES_CACHE_KEY);
    }

    /**
     * Clear settings cache
     */
    public function clearSettingsCache(): void
    {
        Cache::forget(self::SETTINGS_CACHE_KEY);
    }

    /**
     * Clear languages cache
     */
    public function clearLanguagesCache(): void
    {
        Cache::forget(self::LANGUAGES_CACHE_KEY);
    }

    /**
     * Clear all app caches
     */
    public function clearAllCaches(): void
    {
        $this->clearGenresCache();
        $this->clearSettingsCache();
        $this->clearLanguagesCache();
        Cache::forget(self::FEATURED_CONTENT_KEY);
    }

    /**
     * Cache data with custom key and TTL
     */
    public function remember(string $key, $ttl, callable $callback)
    {
        return Cache::remember($key, $ttl, $callback);
    }

    /**
     * Cache user-specific data (shorter TTL)
     */
    public function rememberUser(string $key, callable $callback)
    {
        return Cache::remember($key, 900, $callback); // 15 minutes
    }
} 