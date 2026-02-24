use tauri::{
    plugin::{Builder, TauriPlugin},
    Manager, Runtime,
};

pub use models::*;

#[cfg(desktop)]
mod desktop;
#[cfg(mobile)]
mod mobile;

mod commands;
mod error;
mod models;

pub use error::{Error, Result};

#[cfg(desktop)]
use desktop::A;
#[cfg(mobile)]
use mobile::A;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the a APIs.
pub trait AExt<R: Runtime> {
    fn a(&self) -> &A<R>;
}

impl<R: Runtime, T: Manager<R>> crate::AExt<R> for T {
    fn a(&self) -> &A<R> {
        self.state::<A<R>>().inner()
    }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::new("a")
        .invoke_handler(tauri::generate_handler![commands::take_picture])
        .setup(|app, api| {
            #[cfg(mobile)]
            let a = mobile::init(app, api)?;
            #[cfg(desktop)]
            let a = desktop::init(app, api)?;
            app.manage(a);
            Ok(())
        })
        .build()
}
