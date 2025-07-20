<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Language;
use Illuminate\Http\Request;

class LanguageController extends Controller
{
    public function viewListLanguage()
    {
        return view('admin.language.list');
    }

    public function showLanguageList(Request $request)
    {
        $query = Language::query();
        
        if ($request->has('search') && !empty($request->search)) {
            $query->where('title', 'LIKE', "%{$request->search}%");
        }

        $languages = $query->paginate($request->get('length', 10));
        
        return response()->json([
            'data' => $languages->items(),
            'recordsTotal' => $languages->total(),
            'recordsFiltered' => $languages->total()
        ]);
    }

    public function addUpdateLanguage(Request $request)
    {
        if ($request->id) {
            $language = Language::find($request->id);
        } else {
            $language = new Language();
        }
        
        $language->title = $request->title;
        $language->save();

        return response()->json([
            'status' => true,
            'message' => 'Language saved successfully'
        ]);
    }

    public function deleteLanguage(Request $request)
    {
        $language = Language::find($request->id);
        if ($language) {
            $language->delete();
            return response()->json(['status' => true, 'message' => 'Language deleted successfully']);
        }
        return response()->json(['status' => false, 'message' => 'Language not found']);
    }

    public function CheckExistLanguage(Request $request)
    {
        $exists = Language::where('title', $request->title)
                         ->when($request->id, function($q) use ($request) {
                             $q->where('id', '!=', $request->id);
                         })
                         ->exists();
        
        return response()->json(['exists' => $exists]);
    }
} 