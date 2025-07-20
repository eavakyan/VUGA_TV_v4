<?php

namespace App;

use Illuminate\Database\Eloquent\Model;
use App\CustomAds;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\File;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Storage;

class GlobalFunction extends Model
{

    public static function sendPushToUser($title, $message, $token)
    {
        $url = 'https://fcm.googleapis.com/fcm/send';
        $api_key = env('FCMKEY');
        $notificationArray = array('title' => $title, 'body' => $message, 'sound' => 'default', 'badge' => '1');

        $fields = array('to' => "/token/" . $token, 'notification' => $notificationArray, 'priority' => 'high');
        $headers = array(
            'Content-Type:application/json',
            'Authorization:key=' . $api_key
        );
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
        // print_r(json_encode($fields));
        $result = curl_exec($ch);
        if ($result === FALSE) {
            die('FCM Send Error: ' . curl_error($ch));
            Log::debug(curl_error($ch));
        }
        curl_close($ch);

        if ($result) {
            $response['status'] = true;
            $response['message'] = 'Notification sent successfully !';
        } else {
            $response['status'] = false;
            $response['message'] = 'Something Went Wrong !';
        }
        // echo json_encode($response);
    }

    public static function createMediaUrl($media)
    {
        $url = url(env('DEFAULT_IMAGE_URL') . $media);
        return $url;
    }

    public static function uploadFilToS3($request, $key)
    {
        $s3 = Storage::disk('s3');
        $file = $request->file($key);
        $fileName = time() . $file->getClientOriginalName();
        $fileName = str_replace(" ", "_", $fileName);
        $filePath = 'uploads/' . $fileName;
        $result =  $s3->put($filePath, file_get_contents($file), 'public-read');
        return $filePath;
    }

    public static function cleanString($string)
    {

        return  str_replace(array('<', '>', '{', '}', '[', ']', '`'), '', $string);
    }



    public static function deleteFile($filename)
    {
        if ($filename != null && file_exists(storage_path('app/public/' . $filename))) {
            unlink(storage_path('app/public/' . $filename));
        }
    }

    public static function saveFileAndGivePath($file)
    {
        $mytime = Carbon::now();
        $fileName = $mytime->toDateTimeString()  . $file->getClientOriginalName();
        $fileName = str_replace(array(' ', ':'), '-', $fileName);
        // $fileName =  $file->getClientOriginalName();
        $destinationPath = public_path('uploads/');
        File::makeDirectory($destinationPath, $mode = 0777, true, true);
        $file->move($destinationPath, $fileName);

        return $fileName;
    }

    public static function generateCodeNumber()
    {
        $token =  rand(100000, 999999);

        $first = 'ADS';
        $first .= $token;
        // $first .= 'GER';
        $count = CustomAds::where('campaign_number', $first)->count();

        while ($count >= 1) {

            $token =  rand(100000, 999999);

            $first = GlobalFunction::generateRandomString(3);
            $first .= $token;
            $first .= GlobalFunction::generateRandomString(3);
            $count = CustomAds::where('campaign_number', $first)->count();
        }

        return $first;
    }

    public static function generateRandomString($length)
    {
        $characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        $charactersLength = strlen($characters);
        $randomString = '';
        for ($i = 0; $i < $length; $i++) {
            $randomString .= $characters[rand(0, $charactersLength - 1)];
        }
        return $randomString;
    }
}
