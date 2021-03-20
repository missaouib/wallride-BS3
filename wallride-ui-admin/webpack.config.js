const webpack = require('webpack');
const path = require('path');
// const ExtractTextPlugin = require('extract-text-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: "./src/app.js",
	output: {
		path: path.resolve(__dirname, "dist"),
		filename: 'resources/admin/bundle.js'
	},
	module: {
		rules: [
			{
				test: /\.css$/,
				use: [
					{
						loader: MiniCssExtractPlugin.loader,
						options: {
							publicPath: '',
						},
					},
					'css-loader',
				] 
				// use: [MiniCssExtractPlugin.loader, 'css-loader'],
				// use: ExtractTextPlugin.extract({
				// 	fallback: "style-loader",
				// 	use: "css-loader"
				// })
			},
			{
				test: /\.(jpg|png|gif)$/,
				use: 'url-loader',
			},
			{
				test: /\.(ttf|otf|eot|svg|woff2?)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				exclude: /node_modules/,
				loader: 'file-loader',
				options: {
					context: path.resolve(__dirname, 'src/resources'),
					name: '[path][name].[ext]',
					// publicPath: './font/',
					emitFile: false
				}
			}
		]
	},
	plugins: [
		new webpack.ProvidePlugin({
			$: "jquery",
			jQuery: "jquery",
			"window.jQuery": "jquery",
			"global.jQuery": "jquery",
			process: 'process/browser',
			Buffer: 'buffer/index' 
		}),
		// new ExtractTextPlugin("resources/admin/bundle.css"),
		new MiniCssExtractPlugin({
			filename: 'resources/admin/bundle.css',
			chunkFilename: 'resources/admin/[name].css'
		}),
		new CopyWebpackPlugin({
			patterns:[
				// { from: 'node_modules/bootstrap/dist/fonts/*', to: 'resources/admin' },	// node_...目錄(包括目錄)拷貝到...
				{ context: 'src/resources', from: 'css/wallride.custom.css', to: 'resources/admin/css' },
				{ context: 'src/resources', from: 'font/**/*', to: 'resources/admin' },
				{ context: 'src/resources', from: 'img/**/*', to: 'resources/admin' },	// 目錄 img 下所有(包括img)拷貝到...
				{ context: 'src/templates', from: '**/*', to: 'templates/admin' }	// 目錄 templates 下所有(不包括templates)拷貝到...
			],
		})
	],
	devServer: {
		contentBase: [
			path.resolve(__dirname, "src/templates"),
			path.resolve(__dirname, "src/resources")
		],
		port: 8000
	}, 
	resolve: {
	　fallback: {
			fs: false,
	　　"util": require.resolve("util/"),
	　　"os": require.resolve("os-browserify/browser"),
	　　"crypto": require.resolve("crypto-browserify"),
	　　"buffer": require.resolve("buffer/"),
	　　"path": require.resolve("path-browserify"),
	　　"zlib": require.resolve("browserify-zlib"),
	　　"http": require.resolve("stream-http"),
	　　"https": require.resolve("https-browserify"),
	　　"assert": require.resolve("assert/"),
	　　"stream": require.resolve("stream-browserify")
	　}
	} 
};