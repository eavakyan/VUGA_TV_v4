<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\V2\LiveTvSchedule;
use App\Models\V2\TvChannel;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;

class LiveTvScheduleController extends Controller
{
    public function index(Request $request)
    {
        $channels = TvChannel::active()->orderBy('channel_number')->get();
        $selectedDate = $request->get('date', now()->format('Y-m-d'));
        $selectedChannel = $request->get('channel_id');
        
        return view('admin.live-tv.schedule.index', compact('channels', 'selectedDate', 'selectedChannel'));
    }

    public function getScheduleGrid(Request $request)
    {
        $date = $request->get('date', now()->format('Y-m-d'));
        $channelId = $request->get('channel_id');
        
        $startOfDay = Carbon::parse($date)->startOfDay();
        $endOfDay = Carbon::parse($date)->endOfDay();
        
        $query = LiveTvSchedule::with('channel')
                              ->whereBetween('start_time', [$startOfDay, $endOfDay]);
        
        if ($channelId) {
            $query->where('tv_channel_id', $channelId);
        }
        
        $schedules = $query->orderBy('start_time')->get();
        
        // Group by channel for grid display
        $scheduleGrid = $schedules->groupBy('tv_channel_id')->map(function ($channelSchedules) {
            return $channelSchedules->map(function ($schedule) {
                return [
                    'schedule_id' => $schedule->schedule_id,
                    'program_title' => $schedule->program_title,
                    'description' => $schedule->description,
                    'start_time' => $schedule->start_time->format('H:i'),
                    'end_time' => $schedule->end_time->format('H:i'),
                    'duration_minutes' => $schedule->duration_in_minutes,
                    'is_currently_airing' => $schedule->is_currently_airing,
                    'genre' => $schedule->genre,
                    'rating' => $schedule->rating,
                    'is_repeat' => $schedule->is_repeat,
                    'episode_info' => $schedule->season_number && $schedule->episode_number 
                        ? "S{$schedule->season_number}E{$schedule->episode_number}" 
                        : null,
                    'thumbnail_url' => $schedule->thumbnail_url,
                ];
            });
        });
        
        return response()->json([
            'success' => true,
            'data' => $scheduleGrid,
            'date' => $date
        ]);
    }

    public function getScheduleCalendar(Request $request)
    {
        $month = $request->get('month', now()->month);
        $year = $request->get('year', now()->year);
        $channelId = $request->get('channel_id');
        
        $startOfMonth = Carbon::createFromDate($year, $month, 1)->startOfMonth();
        $endOfMonth = Carbon::createFromDate($year, $month, 1)->endOfMonth();
        
        $query = LiveTvSchedule::with('channel')
                              ->whereBetween('start_time', [$startOfMonth, $endOfMonth]);
        
        if ($channelId) {
            $query->where('tv_channel_id', $channelId);
        }
        
        $schedules = $query->get();
        
        // Group by date for calendar display
        $calendarData = $schedules->groupBy(function ($schedule) {
            return $schedule->start_time->format('Y-m-d');
        })->map(function ($daySchedules) {
            return $daySchedules->map(function ($schedule) {
                return [
                    'schedule_id' => $schedule->schedule_id,
                    'program_title' => $schedule->program_title,
                    'channel_title' => $schedule->channel->title,
                    'start_time' => $schedule->start_time->format('H:i'),
                    'end_time' => $schedule->end_time->format('H:i'),
                    'genre' => $schedule->genre,
                    'is_repeat' => $schedule->is_repeat,
                ];
            });
        });
        
        return response()->json([
            'success' => true,
            'data' => $calendarData,
            'month' => $month,
            'year' => $year
        ]);
    }

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|exists:tv_channel,tv_channel_id',
            'program_title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'start_time' => 'required|date',
            'end_time' => 'required|date|after:start_time',
            'genre' => 'nullable|string|max:100',
            'thumbnail_url' => 'nullable|url',
            'rating' => 'nullable|string|max:10',
            'season_number' => 'nullable|integer|min:1',
            'episode_number' => 'nullable|integer|min:1',
            'original_air_year' => 'nullable|integer|min:1900|max:' . (date('Y') + 10),
            'is_repeat' => 'boolean',
            'metadata' => 'nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        // Check for overlapping schedules
        $startTime = Carbon::parse($request->start_time);
        $endTime = Carbon::parse($request->end_time);
        
        $overlapping = LiveTvSchedule::where('tv_channel_id', $request->tv_channel_id)
            ->where(function ($query) use ($startTime, $endTime) {
                $query->whereBetween('start_time', [$startTime, $endTime->subSecond()])
                      ->orWhereBetween('end_time', [$startTime->addSecond(), $endTime])
                      ->orWhere(function ($q) use ($startTime, $endTime) {
                          $q->where('start_time', '<=', $startTime)
                            ->where('end_time', '>=', $endTime);
                      });
            })->exists();

        if ($overlapping) {
            return response()->json([
                'success' => false,
                'message' => 'Schedule conflicts with existing program in the same time slot'
            ], 422);
        }

        try {
            $scheduleData = $request->only([
                'tv_channel_id', 'program_title', 'description', 'start_time', 
                'end_time', 'genre', 'thumbnail_url', 'rating', 'season_number', 
                'episode_number', 'original_air_year', 'metadata'
            ]);

            $scheduleData['is_repeat'] = $request->has('is_repeat');

            $schedule = LiveTvSchedule::create($scheduleData);

            return response()->json([
                'success' => true,
                'message' => 'Schedule created successfully',
                'data' => $schedule->load('channel')
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to create schedule: ' . $e->getMessage()
            ], 500);
        }
    }

    public function show($id)
    {
        try {
            $schedule = LiveTvSchedule::with('channel')->findOrFail($id);
            return response()->json([
                'success' => true,
                'data' => $schedule
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Schedule not found'
            ], 404);
        }
    }

    public function update(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'tv_channel_id' => 'required|exists:tv_channel,tv_channel_id',
            'program_title' => 'required|string|max:255',
            'description' => 'nullable|string',
            'start_time' => 'required|date',
            'end_time' => 'required|date|after:start_time',
            'genre' => 'nullable|string|max:100',
            'thumbnail_url' => 'nullable|url',
            'rating' => 'nullable|string|max:10',
            'season_number' => 'nullable|integer|min:1',
            'episode_number' => 'nullable|integer|min:1',
            'original_air_year' => 'nullable|integer|min:1900|max:' . (date('Y') + 10),
            'is_repeat' => 'boolean',
            'metadata' => 'nullable|array'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $schedule = LiveTvSchedule::findOrFail($id);
            
            // Check for overlapping schedules (excluding current schedule)
            $startTime = Carbon::parse($request->start_time);
            $endTime = Carbon::parse($request->end_time);
            
            $overlapping = LiveTvSchedule::where('tv_channel_id', $request->tv_channel_id)
                ->where('schedule_id', '!=', $id)
                ->where(function ($query) use ($startTime, $endTime) {
                    $query->whereBetween('start_time', [$startTime, $endTime->subSecond()])
                          ->orWhereBetween('end_time', [$startTime->addSecond(), $endTime])
                          ->orWhere(function ($q) use ($startTime, $endTime) {
                              $q->where('start_time', '<=', $startTime)
                                ->where('end_time', '>=', $endTime);
                          });
                })->exists();

            if ($overlapping) {
                return response()->json([
                    'success' => false,
                    'message' => 'Schedule conflicts with existing program in the same time slot'
                ], 422);
            }

            $scheduleData = $request->only([
                'tv_channel_id', 'program_title', 'description', 'start_time', 
                'end_time', 'genre', 'thumbnail_url', 'rating', 'season_number', 
                'episode_number', 'original_air_year', 'metadata'
            ]);

            $scheduleData['is_repeat'] = $request->has('is_repeat');

            $schedule->update($scheduleData);

            return response()->json([
                'success' => true,
                'message' => 'Schedule updated successfully',
                'data' => $schedule->fresh('channel')
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to update schedule: ' . $e->getMessage()
            ], 500);
        }
    }

    public function destroy($id)
    {
        try {
            $schedule = LiveTvSchedule::findOrFail($id);
            $schedule->delete();

            return response()->json([
                'success' => true,
                'message' => 'Schedule deleted successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to delete schedule: ' . $e->getMessage()
            ], 500);
        }
    }

    public function bulkImport(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'import_file' => 'required|file|mimes:csv,json|max:10240',
            'tv_channel_id' => 'nullable|exists:tv_channel,tv_channel_id',
            'import_format' => 'required|in:csv,json'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $file = $request->file('import_file');
            $format = $request->import_format;
            $channelId = $request->tv_channel_id;
            
            $importedCount = 0;
            $skippedCount = 0;
            $errors = [];

            if ($format === 'csv') {
                $importedCount = $this->importFromCsv($file, $channelId, $errors, $skippedCount);
            } else {
                $importedCount = $this->importFromJson($file, $channelId, $errors, $skippedCount);
            }

            return response()->json([
                'success' => true,
                'message' => "Import completed. {$importedCount} schedules imported, {$skippedCount} skipped.",
                'imported_count' => $importedCount,
                'skipped_count' => $skippedCount,
                'errors' => $errors
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Import failed: ' . $e->getMessage()
            ], 500);
        }
    }

    private function importFromCsv($file, $channelId, &$errors, &$skippedCount)
    {
        $csvData = array_map('str_getcsv', file($file->getRealPath()));
        $headers = array_shift($csvData);
        $importedCount = 0;

        foreach ($csvData as $row) {
            $data = array_combine($headers, $row);
            
            try {
                $scheduleData = [
                    'tv_channel_id' => $channelId ?: $data['channel_id'],
                    'program_title' => $data['program_title'],
                    'description' => $data['description'] ?? null,
                    'start_time' => Carbon::parse($data['start_time']),
                    'end_time' => Carbon::parse($data['end_time']),
                    'genre' => $data['genre'] ?? null,
                    'rating' => $data['rating'] ?? null,
                    'is_repeat' => filter_var($data['is_repeat'] ?? false, FILTER_VALIDATE_BOOLEAN)
                ];

                LiveTvSchedule::create($scheduleData);
                $importedCount++;

            } catch (\Exception $e) {
                $skippedCount++;
                $errors[] = "Row {$importedCount + $skippedCount}: " . $e->getMessage();
            }
        }

        return $importedCount;
    }

    private function importFromJson($file, $channelId, &$errors, &$skippedCount)
    {
        $jsonData = json_decode(file_get_contents($file->getRealPath()), true);
        $importedCount = 0;

        foreach ($jsonData as $data) {
            try {
                $scheduleData = [
                    'tv_channel_id' => $channelId ?: $data['channel_id'],
                    'program_title' => $data['program_title'],
                    'description' => $data['description'] ?? null,
                    'start_time' => Carbon::parse($data['start_time']),
                    'end_time' => Carbon::parse($data['end_time']),
                    'genre' => $data['genre'] ?? null,
                    'rating' => $data['rating'] ?? null,
                    'is_repeat' => $data['is_repeat'] ?? false,
                    'season_number' => $data['season_number'] ?? null,
                    'episode_number' => $data['episode_number'] ?? null,
                    'original_air_year' => $data['original_air_year'] ?? null,
                    'metadata' => $data['metadata'] ?? null
                ];

                LiveTvSchedule::create($scheduleData);
                $importedCount++;

            } catch (\Exception $e) {
                $skippedCount++;
                $errors[] = "Item {$importedCount + $skippedCount}: " . $e->getMessage();
            }
        }

        return $importedCount;
    }

    public function downloadTemplate(Request $request)
    {
        $format = $request->get('format', 'csv');
        
        if ($format === 'csv') {
            $headers = [
                'channel_id', 'program_title', 'description', 'start_time', 'end_time',
                'genre', 'rating', 'is_repeat', 'season_number', 'episode_number', 
                'original_air_year'
            ];
            
            $sampleData = [
                [1, 'Sample Program', 'This is a sample program description', '2024-01-01 10:00:00', '2024-01-01 11:00:00', 'News', 'PG', 'false', '', '', ''],
                [1, 'Another Program', 'Another sample program', '2024-01-01 11:00:00', '2024-01-01 12:30:00', 'Drama', 'PG-13', 'false', '1', '5', '2023']
            ];
            
            $filename = 'schedule_import_template.csv';
            $handle = fopen('php://temp', 'r+');
            fputcsv($handle, $headers);
            foreach ($sampleData as $row) {
                fputcsv($handle, $row);
            }
            rewind($handle);
            $content = stream_get_contents($handle);
            fclose($handle);
            
            return response($content)
                ->header('Content-Type', 'text/csv')
                ->header('Content-Disposition', "attachment; filename=\"{$filename}\"");
                
        } else {
            $sampleData = [
                [
                    'channel_id' => 1,
                    'program_title' => 'Sample Program',
                    'description' => 'This is a sample program description',
                    'start_time' => '2024-01-01 10:00:00',
                    'end_time' => '2024-01-01 11:00:00',
                    'genre' => 'News',
                    'rating' => 'PG',
                    'is_repeat' => false,
                    'season_number' => null,
                    'episode_number' => null,
                    'original_air_year' => null,
                    'metadata' => null
                ],
                [
                    'channel_id' => 1,
                    'program_title' => 'Another Program',
                    'description' => 'Another sample program',
                    'start_time' => '2024-01-01 11:00:00',
                    'end_time' => '2024-01-01 12:30:00',
                    'genre' => 'Drama',
                    'rating' => 'PG-13',
                    'is_repeat' => false,
                    'season_number' => 1,
                    'episode_number' => 5,
                    'original_air_year' => 2023,
                    'metadata' => ['additional_info' => 'Sample metadata']
                ]
            ];
            
            $filename = 'schedule_import_template.json';
            return response(json_encode($sampleData, JSON_PRETTY_PRINT))
                ->header('Content-Type', 'application/json')
                ->header('Content-Disposition', "attachment; filename=\"{$filename}\"");
        }
    }

    public function duplicateSchedule(Request $request, $id)
    {
        $validator = Validator::make($request->all(), [
            'target_dates' => 'required|array',
            'target_dates.*' => 'date',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation failed',
                'errors' => $validator->errors()
            ], 422);
        }

        try {
            $originalSchedule = LiveTvSchedule::findOrFail($id);
            $targetDates = $request->target_dates;
            $duplicatedCount = 0;
            $errors = [];

            foreach ($targetDates as $targetDate) {
                try {
                    $targetDateTime = Carbon::parse($targetDate);
                    $originalDateTime = Carbon::parse($originalSchedule->start_time);
                    
                    $timeDiff = $originalDateTime->diffInMinutes($originalSchedule->end_time);
                    
                    $newStartTime = $targetDateTime->copy()
                        ->hour($originalDateTime->hour)
                        ->minute($originalDateTime->minute)
                        ->second($originalDateTime->second);
                    
                    $newEndTime = $newStartTime->copy()->addMinutes($timeDiff);

                    $duplicateData = $originalSchedule->toArray();
                    unset($duplicateData['schedule_id'], $duplicateData['created_at'], $duplicateData['updated_at']);
                    
                    $duplicateData['start_time'] = $newStartTime;
                    $duplicateData['end_time'] = $newEndTime;

                    LiveTvSchedule::create($duplicateData);
                    $duplicatedCount++;

                } catch (\Exception $e) {
                    $errors[] = "Failed to duplicate to {$targetDate}: " . $e->getMessage();
                }
            }

            return response()->json([
                'success' => true,
                'message' => "Successfully duplicated schedule to {$duplicatedCount} date(s)",
                'duplicated_count' => $duplicatedCount,
                'errors' => $errors
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Failed to duplicate schedule: ' . $e->getMessage()
            ], 500);
        }
    }
}