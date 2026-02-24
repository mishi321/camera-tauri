use tauri::{AppHandle, Runtime};

use crate::models::*;
use crate::Result;
use crate::AExt;

#[tauri::command]
pub(crate) async fn take_picture<R: Runtime>(
    app: AppHandle<R>,
    payload: TakePictureRequest,
) -> Result<TakePictureResponse> {
    app.a().take_picture(payload)
}