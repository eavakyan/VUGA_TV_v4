<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Models\Profile;

class FixAvatarUrls extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'avatars:fix-urls';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Fix double slash issue in avatar URLs';

    /**
     * Execute the console command.
     *
     * @return int
     */
    public function handle()
    {
        $profiles = Profile::whereNotNull('custom_avatar_url')
            ->where('custom_avatar_url', 'like', '%//%')
            ->where('custom_avatar_url', 'not like', 'http://%')
            ->where('custom_avatar_url', 'not like', 'https://%')
            ->get();

        $this->info('Found ' . $profiles->count() . ' profiles with double slash in URLs');

        foreach ($profiles as $profile) {
            $oldUrl = $profile->custom_avatar_url;
            // Fix double slashes except after http: or https:
            $newUrl = preg_replace('#(?<!:)//#', '/', $oldUrl);
            
            if ($oldUrl !== $newUrl) {
                $profile->custom_avatar_url = $newUrl;
                $profile->save();
                $this->info('Fixed: ' . $oldUrl . ' -> ' . $newUrl);
            }
        }

        $this->info('Avatar URLs fixed successfully');
        return 0;
    }
}