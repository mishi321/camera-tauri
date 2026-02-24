use serde::de::DeserializeOwned;
use tauri::{
    plugin::{PluginApi, PluginHandle},
    AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_a);

// initializes the Kotlin or Swift plugin classes
pub fn init<R: Runtime, C: DeserializeOwned>(
    _app: &AppHandle<R>,
    api: PluginApi<R, C>,
) -> crate::Result<A<R>> {
    #[cfg(target_os = "android")]
    let handle = api.register_android_plugin("camera.tauri.a", "ExamplePlugin")?;
    #[cfg(target_os = "ios")]
    let handle = api.register_ios_plugin(init_plugin_a)?;
    Ok(A(handle))
}

/// Access to the a APIs.
pub struct A<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> A<R> {
    pub fn take_picture(&self, payload: TakePictureRequest) -> crate::Result<TakePictureResponse> {
        self.0
            .run_mobile_plugin("take_picture", payload)
            .map_err(Into::into)
    }
}
