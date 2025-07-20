<?php

namespace App;

use Illuminate\Database\Eloquent\Model;
use File;
use DB;
use DateTime;

class Common extends Model
{

    public static function InitialAvtar($name)
    {

        $fullname = explode(" ", $name);
        $count = count($fullname);
        if ($count >= 3) {
            $firstname = $fullname[count($fullname) - 3];
            $middlename = $fullname[count($fullname) - 2];
            $lastname = $fullname[count($fullname) - 1];
        } else if ($count == 2) {
            $firstname = $fullname[count($fullname) - 2];
            $lastname = $fullname[count($fullname) - 1];
        } else {
            $firstname = $name;
        }

        if (!empty($firstname) && !empty($lastname)) {
            $fullname = $firstname . "+" . $lastname;
        } else {
            $fullname = $firstname;
        }

        return "https://ui-avatars.com/api/?name=" . $fullname . "&length=1&rounded=true&background=fdd900&color=000";
    }

    public static function time_elapsed_string($datetime, $full = false)
    {
        $now = new DateTime;
        $ago = new DateTime($datetime);
        $diff = $now->diff($ago);

        $diff->w = floor($diff->d / 7);
        $diff->d -= $diff->w * 7;

        $string = array(
            'y' => 'year',
            'm' => 'month',
            'w' => 'week',
            'd' => 'day',
            'h' => 'hour',
            'i' => 'minute',
            's' => 'second',
        );
        foreach ($string as $k => &$v) {
            if ($diff->$k) {
                $v = $diff->$k . ' ' . $v . ($diff->$k > 1 ? 's' : '');
            } else {
                unset($string[$k]);
            }
        }

        if (!$full) $string = array_slice($string, 0, 1);
        return $string ? implode(', ', $string) . ' ago' : 'just now';
    }

    public static function number_format_short($n)
    {
        if ($n > 0 && $n < 1000) {
            // 1 - 999
            $n_format = floor($n);
            $suffix = '';
        } else if ($n >= 1000 && $n < 1000000) {
            // 1k-999k
            $n_format = floor($n / 1000);
            $suffix = 'K+';
        } else if ($n >= 1000000 && $n < 1000000000) {
            // 1m-999m
            $n_format = floor($n / 1000000);
            $suffix = 'M+';
        } else if ($n >= 1000000000 && $n < 1000000000000) {
            // 1b-999b
            $n_format = floor($n / 1000000000);
            $suffix = 'B+';
        } else if ($n >= 1000000000000) {
            // 1t+
            $n_format = floor($n / 1000000000000);
            $suffix = 'T+';
        }

        return !empty($n_format . $suffix) ? $n_format . $suffix : 0;
    }

    public static function send_push($topic, $title = "Flixy", $message, $plateform = "", $image = "", $flag = 0)
    {
        if ($flag == 1) {

            $customData =  array("message" => $message);

            $url = 'https://fcm.googleapis.com/fcm/send';

            $api_key = env('FCM_TOKEN');

            // $fields = array (
            //     'registration_ids' => array (
            //         $topic
            //     ),
            //     'data' => $customData
            // );

            $body = $message;
            $notification = array('title' => $title, 'body' => $body, 'sound' => 'default', 'badge' => '1');
            $fields = array('to' => '/topics/flixy', 'notification' => $notification, 'priority' => 'high');

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
            }
            curl_close($ch);

            return $result;
        } else {
            if ($plateform == 1) {
                $customData =  array("message" => $message);

                $url = 'https://fcm.googleapis.com/fcm/send';

                $api_key = env('FCM_TOKEN');

                // $fields = array (
                //     'registration_ids' => array (
                //         $topic
                //     ),
                //     'data' => $customData
                // );

                $body = $message;
                $notification = array('title' => $title, 'body' => $body, 'sound' => 'default', 'badge' => '1');
                $fields = array('to' => $topic, 'notification' => $notification, 'priority' => 'high');

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
                }
                curl_close($ch);

                return $result;
            } else {
                $url = 'https://fcm.googleapis.com/fcm/send';

                $api_key = env('FCM_TOKEN');

                $msg = array('title' => $title, 'body' => $message);

                $message = array(
                    "message" => $title,
                    "data" => $message,
                );

                $data = array('registration_ids' => array($topic));
                $data['data'] = $message;
                $data['notification'] = $msg;
                $data['notification']['sound'] = "default";

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
                curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
                //echo json_encode($data);
                $result = curl_exec($ch);
                if ($result === FALSE) {
                    die('FCM Send Error: ' . curl_error($ch));
                }
                curl_close($ch);
                // print_r($result);
                return $result;
            }
        }
    }


    public static function GetContentDataByGenre($data, $user_id)
    {

        $Contentlist = [];
        $i = 0;
        foreach ($data as $catkey => $gvalue) {
            $ContentData =  Content::select('*')
                ->whereRaw("FIND_IN_SET( " . $gvalue['genre_id'] . " , genre_id) ")
                ->orderBy('id', 'DESC')
                ->offset(0)
                ->limit(10)
                ->get();
            if ($ContentData) {
                foreach ($ContentData as $key => $value) {
                    $list['id'] = $value['id'];
                    $list['content_id'] = $value['content_id'];
                    $list['content_type'] = $value['content_type'];
                    $list['content_title'] = $value['content_title'];
                    $list['verticle_poster'] = $value['verticle_poster'];
                    $list['horizontal_poster'] = $value['horizontal_poster'];

                    $Contentlist[$gvalue['genre_id']]['genre_id'] = $gvalue['genre_id'];
                    $Contentlist[$gvalue['genre_id']]['genre_name'] = $gvalue['genre_name'];
                    $Contentlist[$gvalue['genre_id']]['content'][] = $list;
                }
            }
        }
        return $Contentlist;
    }


    public static function GetTVChannelDataByCategory($data, $user_id)
    {

        $TVChannellist = [];
        $i = 0;
        foreach ($data as $catkey => $gvalue) {
            $TVChannelData =  TVChannel::select('*')
                ->whereRaw("FIND_IN_SET( " . $gvalue['category_id'] . " , category_id) ")
                ->orderBy('id', 'DESC')
                ->offset(0)
                ->limit(5)
                ->get();
            if ($TVChannelData) {
                foreach ($TVChannelData as $key => $value) {
                    // $sourceData = TVChannelSource::where('channel_id',$value['channel_id'])->first();
                    $list['id'] = $value['id'];
                    $list['channel_id'] = $value['channel_id'];
                    $list['channel_title'] = $value['channel_title'];
                    $list['channel_thumb'] = $value['channel_thumb'];
                    $list['access_type'] = $value['access_type'];
                    $list['source_type'] = $value['source_type'];
                    $list['source'] = $value['source'];

                    $TVChannellist[$gvalue['category_id']]['category_id'] = $gvalue['category_id'];
                    $TVChannellist[$gvalue['category_id']]['category_name'] = $gvalue['category_name'];
                    $TVChannellist[$gvalue['category_id']]['category_image'] = $gvalue['category_image'];
                    $TVChannellist[$gvalue['category_id']]['tv_channel'][] = $list;
                }
            }
        }
        return $TVChannellist;
    }
}
