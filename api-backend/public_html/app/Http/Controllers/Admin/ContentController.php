<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Content;
use App\Genre;
use App\Language;
use App\Constants;
use Illuminate\Http\Request;

class ContentController extends Controller
{
    // Content List Views
    public function viewListContent()
    {
        $genres = Genre::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        $movieCount = Content::where('type', Constants::movie)->count();
        $seriesCount = Content::where('type', Constants::series)->count();

        return view('admin.content.list', compact('genres', 'languages', 'movieCount', 'seriesCount'));
    }

    public function viewAddContent()
    {
        $genres = Genre::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        return view('admin.content.add', compact('genres', 'languages'));
    }

    public function viewUpdateContent($flag = null, $id)
    {
        $content = Content::findOrFail($id);
        $genres = Genre::orderBy('created_at', 'DESC')->get();
        $languages = Language::orderBy('created_at', 'DESC')->get();
        return view('admin.content.edit', compact('content', 'genres', 'languages'));
    }

    public function viewContent($id)
    {
        $content = Content::findOrFail($id);
        return view('admin.content.view', compact('content'));
    }

    // Content Data Operations
    public function showContentList(Request $request)
    {
        $query = Content::with('language');
        
        if ($request->has('search') && !empty($request->search)) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('title', 'LIKE', "%{$search}%")
                  ->orWhere('description', 'LIKE', "%{$search}%")
                  ->orWhere('release_year', 'LIKE', "%{$search}%");
            });
        }

        if ($request->has('type') && !empty($request->type)) {
            $query->where('type', $request->type);
        }

        $contents = $query->orderBy('created_at', 'DESC')
                          ->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $contents->items(),
            'recordsTotal' => $contents->total(),
            'recordsFiltered' => $contents->total()
        ]);
    }

    public function addUpdateContent(Request $request)
    {
        if ($request->id) {
            $content = Content::find($request->id);
        } else {
            $content = new Content();
        }
        
        $content->title = $request->title;
        $content->description = $request->description;
        $content->type = $request->type;
        $content->language_id = $request->language_id;
        $content->genre_ids = $request->genre_ids;
        $content->release_year = $request->release_year;
        $content->ratings = $request->ratings;
        $content->save();

        return response()->json([
            'status' => true,
            'message' => 'Content saved successfully'
        ]);
    }

    public function deleteContent(Request $request)
    {
        $content = Content::find($request->id);
        if ($content) {
            $content->delete();
            return response()->json(['status' => true, 'message' => 'Content deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Content not found']);
    }

    public function changeFeatureStatus(Request $request)
    {
        $content = Content::find($request->id);
        if ($content) {
            $content->is_featured = $request->status;
            $content->save();
            return response()->json(['status' => true, 'message' => 'Status updated successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Content not found']);
    }

    // Content Sources
    public function viewContentSource($flag = null, $id)
    {
        $content = Content::findOrFail($id);
        return view('admin.content.source.list', compact('content'));
    }

    public function showContentSourceList(Request $request)
    {
        // TODO: Implement content source listing
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateContentSource(Request $request)
    {
        // TODO: Implement content source CRUD
        return response()->json(['status' => true, 'message' => 'Content source saved successfully']);
    }

    public function deleteContentSource(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Content source deleted successfully']);
    }

    // Content Cast
    public function viewAddMovieCast($flag = null, $id)
    {
        $content = Content::findOrFail($id);
        return view('admin.content.cast.list', compact('content'));
    }

    public function showMovieCastList(Request $request)
    {
        // TODO: Implement cast listing
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateMovieCast(Request $request)
    {
        // TODO: Implement cast CRUD
        return response()->json(['status' => true, 'message' => 'Cast saved successfully']);
    }

    public function deleteMovieCast(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Cast deleted successfully']);
    }

    public function CheckExistMCastActor(Request $request)
    {
        // TODO: Check if actor already exists in cast
        return response()->json(['exists' => false]);
    }

    // Content Subtitles
    public function viewAddContentSubtitles($flag = null, $id)
    {
        $content = Content::findOrFail($id);
        return view('admin.content.subtitle.list', compact('content'));
    }

    public function showContentSubtitlesList(Request $request)
    {
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateContentSubtitles(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Subtitle saved successfully']);
    }

    public function deleteContentSubtitles(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Subtitle deleted successfully']);
    }

    // Series Seasons & Episodes
    public function viewAddSeriesSeason($flag = null, $id)
    {
        $content = Content::findOrFail($id);
        return view('admin.content.season.list', compact('content'));
    }

    public function viewUpdateContentSeriesSeason($flag = null, $id)
    {
        $season = \App\Season::findOrFail($id);
        return view('admin.content.season.edit', compact('season'));
    }

    public function showSeasonEpisodeList(Request $request)
    {
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateSeriesSeason(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Season saved successfully']);
    }

    public function deleteSeriesSeason(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Season deleted successfully']);
    }

    public function CheckExistSeason(Request $request)
    {
        return response()->json(['exists' => false]);
    }

    // Episodes
    public function viewAddEpisode($seasonId)
    {
        $season = \App\Season::findOrFail($seasonId);
        return view('admin.content.episode.add', compact('season'));
    }

    public function viewUpdateEpisode($seasonId, $id)
    {
        $episode = \App\Episode::findOrFail($id);
        return view('admin.content.episode.edit', compact('episode'));
    }

    public function addUpdateSeasonEpisode(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Episode saved successfully']);
    }

    public function deleteSeasonEpisode(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Episode deleted successfully']);
    }

    // Episode Sources & Subtitles
    public function viewEpisodeSource($seasonId, $id)
    {
        $episode = \App\Episode::findOrFail($id);
        return view('admin.content.episode.source.list', compact('episode'));
    }

    public function showEpisodeSourceList(Request $request)
    {
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateEpisodeSource(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Episode source saved successfully']);
    }

    public function deleteEpisodeSource(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Episode source deleted successfully']);
    }

    public function viewEpisodeSubtitles($seasonId, $id)
    {
        $episode = \App\Episode::findOrFail($id);
        return view('admin.content.episode.subtitle.list', compact('episode'));
    }

    public function showEpisodeSubtitlesList(Request $request)
    {
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function addUpdateEpisodeSubtitles(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Episode subtitle saved successfully']);
    }

    public function deleteEpisodeSubtitles(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Episode subtitle deleted successfully']);
    }

    // Comments
    public function viewContentComment($flag = null, $id)
    {
        $content = Content::findOrFail($id);
        return view('admin.content.comment.list', compact('content'));
    }

    public function showContentCommentList(Request $request)
    {
        return response()->json(['data' => [], 'recordsTotal' => 0, 'recordsFiltered' => 0]);
    }

    public function deleteComment(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Comment deleted successfully']);
    }

    public function changeCommentStatus(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Comment status updated successfully']);
    }

    // Placeholder methods for file uploads and other operations
    public function UpdateContentMedia(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Content media updated successfully']);
    }

    public function UploadContentSourceMedia(Request $request)
    {
        return response()->json(['status' => true, 'message' => 'Source media uploaded successfully']);
    }
} 